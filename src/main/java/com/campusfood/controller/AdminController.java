package com.campusfood.controller;

import com.campusfood.entity.Producto;
import com.campusfood.entity.Usuario;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public AdminController(UsuarioRepository usuarioRepository, ProductoRepository productoRepository, PedidoRepository pedidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalPedidos", pedidoRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-estado")
    public String toggleEstadoUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Evitar que el admin se bloquee a sí mismo (opcional)
        if (usuario.getEmail().equals("jefe@utp.edu.pe")) {
            redirectAttributes.addFlashAttribute("error", "No puedes bloquear al administrador principal.");
            return "redirect:/admin/usuarios";
        }

        usuario.setHabilitado(!usuario.isHabilitado());
        usuarioRepository.save(usuario);
        
        String accion = usuario.isHabilitado() ? "desbloqueado" : "bloqueado";
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario " + accion + " correctamente.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/productos")
    public String gestionarProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "admin/productos";
    }

    @PostMapping("/productos/{id}/toggle-estado")
    public String toggleEstadoProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        producto.setActivo(!producto.isActivo());
        productoRepository.save(producto);
        
        String accion = producto.isActivo() ? "activado" : "suspendido";
        redirectAttributes.addFlashAttribute("mensajeExito", "Producto " + accion + " correctamente.");
        return "redirect:/admin/productos";
    }
}
