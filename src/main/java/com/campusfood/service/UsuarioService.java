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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final VerificacionCorreoRepository verificacionCorreoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${campusfood.allowed.email.domain}")
    private String allowedDomain;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          VerificacionCorreoRepository verificacionCorreoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.verificacionCorreoRepository = verificacionCorreoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarEstudiante(RegistroEstudianteDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        String emailDomain = dto.getEmail().substring(dto.getEmail().indexOf("@") + 1);
        if (!emailDomain.equalsIgnoreCase(allowedDomain)) {
            throw new IllegalArgumentException("Solo se permite registro con el dominio institucional: " + allowedDomain);
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setHabilitado(false);

        // Asignar rol ESTUDIANTE por defecto
        Rol rolEstudiante = rolRepository.findByNombre("ESTUDIANTE")
            .orElseGet(() -> rolRepository.save(new Rol("ESTUDIANTE")));
        usuario.getRoles().add(rolEstudiante);

        usuario = usuarioRepository.save(usuario);

        // Generar token de verificación
        String token = UUID.randomUUID().toString();
        VerificacionCorreo verificacion = new VerificacionCorreo(token, usuario, LocalDateTime.now().plusHours(24));
        verificacionCorreoRepository.save(verificacion);

        // Simulamos envío de correo imprimiendo el enlace en consola
        System.out.println("==================================================================");
        System.out.println("SIMULACIÓN DE ENVÍO DE CORREO (Solo para desarrollo local)");
        System.out.println("Para: " + usuario.getEmail());
        System.out.println("Enlace de validación: http://localhost:8080/verificar-correo?token=" + token);
        System.out.println("==================================================================");

        return usuario;
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
        
        // El token se usará solo una vez, lo podemos eliminar o marcar usado.
        verificacionCorreoRepository.delete(verificacion);
        
        return true;
    }
}
