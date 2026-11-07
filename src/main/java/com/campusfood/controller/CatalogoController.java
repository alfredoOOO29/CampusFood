package com.campusfood.controller;

import com.campusfood.repository.CategoriaRepository;
import com.campusfood.repository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class CatalogoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public CatalogoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/catalogo")
    public String verCatalogo(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "categoria", required = false) String categoriaParam,
                              Model model) {
        
        Long categoriaId = null;
        String nombreCategoria = null;

        if (categoriaParam != null && !categoriaParam.trim().isEmpty()) {
            String trimmed = categoriaParam.trim();
            // Si el parámetro recibido es numérico (ID de categoría)
            if (trimmed.matches("\\d+")) {
                try {
                    categoriaId = Long.parseLong(trimmed);
                    var catOpt = categoriaRepository.findById(categoriaId);
                    if (catOpt.isPresent()) {
                        nombreCategoria = catOpt.get().getNombre();
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                // Si el parámetro recibido es el nombre de la categoría (ej. "Bebidas", "Postres")
                var catOpt = categoriaRepository.findByNombreIgnoreCase(trimmed);
                if (catOpt.isPresent()) {
                    categoriaId = catOpt.get().getId();
                    nombreCategoria = catOpt.get().getNombre();
                }
            }
        }

        if (q != null && !q.trim().isEmpty()) {
            model.addAttribute("productos", productoRepository.searchDisponibles(q.trim()));
            model.addAttribute("tituloSeccion", "Resultados para: \"" + q.trim() + "\"");
        } else if (categoriaId != null) {
            model.addAttribute("productos", productoRepository.findByCategoriaDisponibles(categoriaId));
            model.addAttribute("tituloSeccion", nombreCategoria != null ? nombreCategoria : "Categoría");
            model.addAttribute("categoriaActual", categoriaId);
        } else {
            model.addAttribute("productos", productoRepository.findAllDisponibles());
            model.addAttribute("tituloSeccion", "Catálogo General");
        }

        model.addAttribute("categorias", categoriaRepository.findAll());
        return "catalogo";
    }

    @GetMapping("/disponible-ahora")
    public String verDisponibleAhora(Model model) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        
        model.addAttribute("productos", productoRepository.findDisponiblesAhora(hoy, ahora));
        model.addAttribute("tituloSeccion", "Disponible Ahora Mismo");
        model.addAttribute("categorias", categoriaRepository.findAll());
        
        return "catalogo"; // Reutilizamos la misma vista
    }
}
