package com.campusfood.controller;

import com.campusfood.entity.EstadoPedido;
import com.campusfood.entity.Producto;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.security.CustomUserDetails;
import com.campusfood.service.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoController(PedidoService pedidoService, PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    // Mostrar detalle del producto para comprar
    @GetMapping("/producto/{id}")
    public String verProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        model.addAttribute("producto", producto);
        return "producto-detalle";
    }

    // Procesar la compra
    @PostMapping("/panel/comprar/{id}")
    public String comprarProducto(@PathVariable Long id,
                                  @RequestParam Integer cantidad,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            pedidoService.crearPedido(id, userDetails.getId(), cantidad);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Pedido realizado con éxito! Contacta al vendedor.");
            return "redirect:/panel/pedidos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/producto/" + id;
        }
    }

    // Ver mis pedidos (comprador)
    @GetMapping("/panel/pedidos")
    public String misPedidos(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pedidos", pedidoRepository.findByCompradorIdOrderByFechaPedidoDesc(userDetails.getId()));
        return "panel/mis-pedidos";
    }

    // Ver mis ventas (vendedor)
    @GetMapping("/panel/ventas")
    public String misVentas(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pedidos", pedidoRepository.findByVendedorIdOrderByFechaPedidoDesc(userDetails.getId()));
        return "panel/mis-ventas";
    }

    // Actualizar estado del pedido
    @PostMapping("/panel/pedidos/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoPedido estado,
                                   @RequestParam String origen,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            pedidoService.actualizarEstadoPedido(id, estado, userDetails.getId());
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado actualizado a " + estado);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/panel/" + origen;
    }
}
