package com.campusfood.controller;

import com.campusfood.entity.Producto;
import com.campusfood.entity.Usuario;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.UsuarioRepository;
import com.campusfood.repository.ReclamoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final ReclamoRepository reclamoRepository;

    public AdminController(UsuarioRepository usuarioRepository, ProductoRepository productoRepository, 
                           PedidoRepository pedidoRepository, ReclamoRepository reclamoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.reclamoRepository = reclamoRepository;
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

    // GESTIÓN DE RECLAMOS Y STRIKES
    @GetMapping("/reclamos")
    public String gestionarReclamos(Model model) {
        model.addAttribute("reclamos", reclamoRepository.findAllByOrderByFechaCreacionDesc());
        return "admin/reclamos";
    }

    @PostMapping("/reclamos/{id}/aprobar")
    public String aprobarReclamoYDarStrike(@PathVariable Long id, 
                                           @RequestParam(required = false) Integer diasSuspension,
                                           RedirectAttributes redirectAttributes) {
        com.campusfood.entity.Reclamo reclamo = reclamoRepository.findById(id).orElseThrow();
        reclamo.setEstado(com.campusfood.entity.EstadoReclamo.APROBADO);
        reclamoRepository.save(reclamo);
        
        Usuario reportado = reclamo.getReportado();
        reportado.setStrikes(reportado.getStrikes() + 1);
        
        String mensaje = "Strike aplicado a " + reportado.getEmail() + " (" + reportado.getStrikes() + " strikes totales).";
        
        if (reportado.getStrikes() >= 3 || diasSuspension != null) {
            reportado.setHabilitado(false);
            if (diasSuspension != null && diasSuspension > 0) {
                reportado.setFechaFinSuspension(LocalDateTime.now().plusDays(diasSuspension));
                mensaje += " Cuenta suspendida por " + diasSuspension + " días.";
            } else {
                reportado.setFechaFinSuspension(null);
                mensaje += " Cuenta suspendida de forma INDEFINIDA.";
            }
            reportado.setMotivoSuspension("Llegaste al límite de strikes o se aplicó suspensión directa por: " + reclamo.getMotivo());
        }
        
        usuarioRepository.save(reportado);
        redirectAttributes.addFlashAttribute("mensajeExito", mensaje);
        return "redirect:/admin/reclamos";
    }

    @PostMapping("/reclamos/{id}/rechazar")
    public String rechazarReclamoOApelacion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        com.campusfood.entity.Reclamo reclamo = reclamoRepository.findById(id).orElseThrow();
        reclamo.setEstado(com.campusfood.entity.EstadoReclamo.RECHAZADO);
        reclamoRepository.save(reclamo);
        
        redirectAttributes.addFlashAttribute("mensajeExito", "Reclamo o Apelación rechazado y desestimado.");
        return "redirect:/admin/reclamos";
    }
}
