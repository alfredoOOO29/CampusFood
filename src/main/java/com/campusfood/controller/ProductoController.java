package com.campusfood.controller;

import com.campusfood.dto.CrearProductoDTO;
import com.campusfood.repository.CategoriaRepository;
import com.campusfood.security.CustomUserDetails;
import com.campusfood.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/panel/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoService productoService, CategoriaRepository categoriaRepository) {
        this.productoService = productoService;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        if (!model.containsAttribute("productoDTO")) {
            model.addAttribute("productoDTO", new CrearProductoDTO());
        }
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "panel/crear-producto";
    }

    @PostMapping("/nuevo")
    public String procesarCrearProducto(@Valid @ModelAttribute("productoDTO") CrearProductoDTO productoDTO,
                                        BindingResult result,
                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "panel/crear-producto";
        }

        try {
            productoService.crearProducto(productoDTO, userDetails.getId());
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto publicado exitosamente.");
            return "redirect:/panel"; // Temporalmente redirige al panel
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorProducto", e.getMessage());
            redirectAttributes.addFlashAttribute("productoDTO", productoDTO);
            return "redirect:/panel/productos/nuevo";
        }
    }
}
