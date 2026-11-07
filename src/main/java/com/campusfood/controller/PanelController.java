package com.campusfood.controller;

import com.campusfood.entity.EstadoPedido;
import com.campusfood.entity.Pedido;
import com.campusfood.entity.Producto;
import com.campusfood.entity.Usuario;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.ResenaRepository;
import com.campusfood.repository.UsuarioRepository;
import com.campusfood.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/panel")
public class PanelController {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResenaRepository resenaRepository;

    public PanelController(ProductoRepository productoRepository,
                           PedidoRepository pedidoRepository,
                           UsuarioRepository usuarioRepository,
                           ResenaRepository resenaRepository) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.resenaRepository = resenaRepository;
    }

    @GetMapping
    public String verPanel(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long usuarioId = userDetails.getId();
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        // ==========================================
        // 1. MÉTRICAS DE COMPRADOR (USUARIO / ESTUDIANTE)
        // ==========================================
        List<Pedido> misCompras = pedidoRepository.findByCompradorIdOrderByFechaPedidoDesc(usuarioId);
        long totalCompras = misCompras.size();

        long pedidosActivos = misCompras.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE ||
                             p.getEstado() == EstadoPedido.ACEPTADO ||
                             p.getEstado() == EstadoPedido.LISTO)
                .count();

        long pedidosListos = misCompras.stream()
                .filter(p -> p.getEstado() == EstadoPedido.LISTO)
                .count();

        BigDecimal totalGastado = misCompras.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ==========================================
        // 2. MÉTRICAS DE VENDEDOR (EMPRENDEDOR)
        // ==========================================
        List<Producto> misProductos = productoRepository.findByVendedorIdOrderByFechaCreacionDesc(usuarioId);
        List<Pedido> misVentas = pedidoRepository.findByVendedorIdOrderByFechaPedidoDesc(usuarioId);

        BigDecimal ganancias = misVentas.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPedidosRecibidos = misVentas.size();
        long totalProductos = misProductos.size();

        Double calificacionAvg = resenaRepository.findAverageRatingByVendedorId(usuarioId);
        Long totalResenas = resenaRepository.countByVendedorId(usuarioId);
        double calificacion = (calificacionAvg != null) ? Math.round(calificacionAvg * 10.0) / 10.0 : 0.0;

        boolean esVendedor = (totalProductos > 0 || totalPedidosRecibidos > 0);

        // ==========================================
        // 3. PASAR DATOS REALES AL MODELO
        // ==========================================
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreUsuario", userDetails.getNombreCompleto());
        model.addAttribute("emailUsuario", userDetails.getUsername());

        // Compras
        model.addAttribute("misCompras", misCompras);
        model.addAttribute("comprasRecientes", misCompras.stream().limit(5).toList());
        model.addAttribute("totalCompras", totalCompras);
        model.addAttribute("pedidosActivos", pedidosActivos);
        model.addAttribute("pedidosListos", pedidosListos);
        model.addAttribute("totalGastado", totalGastado);

        // Ventas
        model.addAttribute("esVendedor", esVendedor);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalPedidos", totalPedidosRecibidos);
        model.addAttribute("ganancias", ganancias);
        model.addAttribute("calificacion", calificacion);
        model.addAttribute("totalResenas", totalResenas != null ? totalResenas : 0L);
        model.addAttribute("misProductos", misProductos);
        model.addAttribute("pedidosRecientes", misVentas.stream().limit(5).toList());

        return "panel/dashboard";
    }
}
