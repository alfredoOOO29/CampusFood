package com.campusfood.controller;

import com.campusfood.dto.RegistroEstudianteDTO;
import com.campusfood.repository.CategoriaRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final UsuarioService usuarioService;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public HomeController(UsuarioService usuarioService,
                          ProductoRepository productoRepository,
                          CategoriaRepository categoriaRepository) {
        this.usuarioService = usuarioService;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "CampusFood - Inicio");
        model.addAttribute("productosDestacados", productoRepository.findAllDisponibles().stream().limit(4).toList());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "index";
    }

    @GetMapping("/carrito")
    public String verCarrito(Model model) {
        model.addAttribute("title", "Tu Carrito - CampusFood");
        return "carrito";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        if (!model.containsAttribute("registroDTO")) {
            model.addAttribute("registroDTO", new RegistroEstudianteDTO());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroEstudianteDTO registroDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registro";
        }

        try {
            usuarioService.registrarEstudiante(registroDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro exitoso. Hemos enviado un código de verificación de 6 dígitos a tu correo.");
            return "redirect:/verificar-codigo?email=" + (registroDTO.getEmail() != null ? registroDTO.getEmail().trim().toLowerCase() : "");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorRegistro", e.getMessage());
            redirectAttributes.addFlashAttribute("registroDTO", registroDTO);
            return "redirect:/registro";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorRegistro", "Error inesperado al procesar el registro: " + e.getMessage());
            redirectAttributes.addFlashAttribute("registroDTO", registroDTO);
            return "redirect:/registro";
        }
    }

    @GetMapping("/verificar-codigo")
    public String mostrarVerificarCodigo(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email != null ? email.trim() : null);
        return "verificar-codigo";
    }

    @PostMapping("/verificar-codigo")
    public String procesarVerificarCodigo(@RequestParam("email") String email,
                                          @RequestParam("codigo") String codigo,
                                          RedirectAttributes redirectAttributes) {
        boolean verificado = usuarioService.validarCodigo(email, codigo);
        if (verificado) {
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Correo verificado correctamente! Tu cuenta ha sido activada. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorVerificacion", "El código ingresado es incorrecto o ha expirado.");
            return "redirect:/verificar-codigo?email=" + (email != null ? email.trim() : "");
        }
    }

    @PostMapping("/reenviar-codigo")
    public String reenviarCodigo(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.reenviarCodigo(email);
            redirectAttributes.addFlashAttribute("mensajeExito", "Hemos enviado un nuevo código de 6 dígitos a tu correo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorVerificacion", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorVerificacion", "Error al reenviar el código: " + e.getMessage());
        }
        return "redirect:/verificar-codigo?email=" + (email != null ? email.trim() : "");
    }

    @GetMapping("/verificar-correo")
    public String verificarCorreo(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean verificado = usuarioService.verificarCorreo(token);
        if (verificado) {
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Correo verificado correctamente! Ya puedes iniciar sesión.");
        } else {
            redirectAttributes.addFlashAttribute("errorLogin", "El enlace de verificación es inválido o ha expirado.");
        }
        return "redirect:/login";
    }
}
