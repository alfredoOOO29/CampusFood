package com.campusfood.controller;

import com.campusfood.entity.EstadoPedido;
import com.campusfood.entity.Pedido;
import com.campusfood.entity.Producto;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
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

    public PanelController(ProductoRepository productoRepository, PedidoRepository pedidoRepository) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping
    public String verPanel(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long usuarioId = userDetails.getId();
        List<Producto> misProductos = productoRepository.findByVendedorIdOrderByFechaCreacionDesc(usuarioId);
        List<Pedido> misVentas = pedidoRepository.findByVendedorIdOrderByFechaPedidoDesc(usuarioId);

        BigDecimal ganancias = misVentas.stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPedidosRecibidos = misVentas.size();
        long totalProductos = misProductos.size();

        model.addAttribute("nombreUsuario", userDetails.getNombreCompleto());
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalPedidos", totalPedidosRecibidos);
        model.addAttribute("ganancias", ganancias);
        model.addAttribute("misProductos", misProductos);
        model.addAttribute("pedidosRecientes", misVentas.stream().limit(5).toList());

        return "panel/dashboard";
    }
}
