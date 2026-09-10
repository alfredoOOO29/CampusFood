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
    public String verCatalogo(@RequestParam(required = false) String q,
                              @RequestParam(required = false) Long categoria,
                              Model model) {
        
        if (q != null && !q.trim().isEmpty()) {
            model.addAttribute("productos", productoRepository.searchDisponibles(q));
            model.addAttribute("tituloSeccion", "Resultados para: " + q);
        } else if (categoria != null) {
            model.addAttribute("productos", productoRepository.findByCategoriaDisponibles(categoria));
            model.addAttribute("tituloSeccion", "Filtrado por categoría");
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
