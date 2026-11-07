package com.campusfood.service;

import com.campusfood.dto.RegistroEstudianteDTO;
import com.campusfood.entity.Rol;
import com.campusfood.entity.Usuario;
import com.campusfood.entity.VerificacionCorreo;
import com.campusfood.repository.RolRepository;
import com.campusfood.repository.UsuarioRepository;
import com.campusfood.repository.VerificacionCorreoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final VerificacionCorreoRepository verificacionCorreoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${campusfood.allowed.email.domain}")
    private String allowedDomain;

    private final SecureRandom secureRandom = new SecureRandom();

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          VerificacionCorreoRepository verificacionCorreoRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.verificacionCorreoRepository = verificacionCorreoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public Usuario registrarEstudiante(RegistroEstudianteDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        String cleanEmail = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : "";
        if (!cleanEmail.contains("@")) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }

        String emailDomain = cleanEmail.substring(cleanEmail.indexOf("@") + 1);
        String allowedDomainsStr = allowedDomain != null ? allowedDomain.trim() : "utp.edu.pe";
        java.util.List<String> allowedList = java.util.Arrays.stream(allowedDomainsStr.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList();

        boolean domainAllowed = allowedList.isEmpty() 
                || allowedList.contains("*") 
                || allowedList.contains(emailDomain.toLowerCase());

        if (!domainAllowed) {
            throw new IllegalArgumentException("Solo se permite registro con dominios autorizados: " + allowedDomainsStr);
        }

        Optional<Usuario> existenteOpt = usuarioRepository.findByEmailIgnoreCase(cleanEmail);
        Usuario usuario;

        if (existenteOpt.isPresent()) {
            usuario = existenteOpt.get();
            if (usuario.isHabilitado()) {
                throw new IllegalArgumentException("El correo ya se encuentra registrado y activo. Puedes iniciar sesión directamente.");
            }
            // El usuario ya existe pero NO está verificado todavía.
            // Actualizamos sus datos para que pueda completar su verificación sin bloquearse
            usuario.setNombre(dto.getNombre().trim());
            usuario.setApellidos(dto.getApellidos().trim());
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            usuario = usuarioRepository.save(usuario);
        } else {
            usuario = new Usuario();
            usuario.setNombre(dto.getNombre().trim());
            usuario.setApellidos(dto.getApellidos().trim());
            usuario.setEmail(cleanEmail);
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            usuario.setHabilitado(false);

            // Asignar rol ESTUDIANTE por defecto
            Rol rolEstudiante = rolRepository.findByNombre("ESTUDIANTE")
                .orElseGet(() -> rolRepository.save(new Rol("ESTUDIANTE")));
            usuario.getRoles().add(rolEstudiante);

            usuario = usuarioRepository.save(usuario);
        }

        // Generar código numérico de 6 dígitos y token
        generarYEnviarCodigo(usuario);

        return usuario;
    }

    @Transactional
    public boolean validarCodigo(String email, String codigo) {
        if (email == null || codigo == null) {
            return false;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(email.trim());
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.isHabilitado()) {
            return true;
        }

        Optional<VerificacionCorreo> verificacionOpt = verificacionCorreoRepository.findByUsuarioAndCodigo(usuario, codigo.trim());
        if (verificacionOpt.isEmpty()) {
            return false;
        }

        VerificacionCorreo verificacion = verificacionOpt.get();
        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Habilitar usuario
        usuario.setHabilitado(true);
        usuarioRepository.save(usuario);

        // Eliminar código usado
        verificacionCorreoRepository.delete(verificacion);

        return true;
    }

    @Transactional
    public void reenviarCodigo(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar un correo electrónico.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe ninguna cuenta registrada con el correo: " + email));

        if (usuario.isHabilitado()) {
            throw new IllegalArgumentException("Esta cuenta ya está verificada y activa.");
        }

        generarYEnviarCodigo(usuario);
    }

    private void generarYEnviarCodigo(Usuario usuario) {
        // Reusar o actualizar registro de verificación existente para evitar colisiones
        VerificacionCorreo verificacion = verificacionCorreoRepository.findTopByUsuarioOrderByFechaExpiracionDesc(usuario)
                .orElse(new VerificacionCorreo());

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", secureRandom.nextInt(1_000_000));
        String token = UUID.randomUUID().toString();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(15);

        verificacion.setUsuario(usuario);
        verificacion.setCodigo(codigo);
        verificacion.setToken(token);
        verificacion.setFechaExpiracion(fechaExpiracion);
        verificacionCorreoRepository.save(verificacion);

        // Enviar por correo mediante EmailService
        emailService.enviarCodigoVerificacion(usuario.getEmail(), usuario.getNombre(), codigo);
    }

    @Transactional
    public boolean verificarCorreo(String token) {
        Optional<VerificacionCorreo> verificacionOpt = verificacionCorreoRepository.findByToken(token);
        if (verificacionOpt.isEmpty()) {
            return false;
        }

        VerificacionCorreo verificacion = verificacionOpt.get();
        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }

        Usuario usuario = verificacion.getUsuario();
        usuario.setHabilitado(true);
        usuarioRepository.save(usuario);
        
        verificacionCorreoRepository.delete(verificacion);
        return true;
    }
}
