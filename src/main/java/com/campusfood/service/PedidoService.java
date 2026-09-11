package com.campusfood.service;

import com.campusfood.entity.*;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Pedido crearPedido(Long productoId, Long compradorId, Integer cantidad) {
        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new IllegalArgumentException("Comprador no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (!producto.isActivo() || producto.getStock() < cantidad) {
            throw new IllegalArgumentException("No hay stock suficiente o el producto no está disponible");
        }

        if (producto.getVendedor().getId().equals(compradorId)) {
            throw new IllegalArgumentException("No puedes comprar tu propio producto");
        }

        // Deducir stock
        producto.setStock(producto.getStock() - cantidad);
        if (producto.getStock() == 0) {
            producto.setActivo(false); // Automáticamente inactivo si se agota
        }
        productoRepository.save(producto);

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setComprador(comprador);
        pedido.setVendedor(producto.getVendedor());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Crear detalle
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());
        
        BigDecimal subtotal = producto.getPrecio().multiply(new BigDecimal(cantidad));
        detalle.setSubtotal(subtotal);

        pedido.addDetalle(detalle);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado, Long usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        boolean esComprador = pedido.getComprador().getId().equals(usuarioId);
        boolean esVendedor = pedido.getVendedor().getId().equals(usuarioId);

        if (!esComprador && !esVendedor) {
            throw new IllegalArgumentException("No tienes permiso para modificar este pedido");
        }

        // Lógica de transición de estados básicos
        if (esVendedor) {
            if (nuevoEstado == EstadoPedido.ACEPTADO || nuevoEstado == EstadoPedido.RECHAZADO || nuevoEstado == EstadoPedido.LISTO) {
                pedido.setEstado(nuevoEstado);
            } else {
                throw new IllegalArgumentException("El vendedor no puede cambiar a este estado");
            }
        }

        if (esComprador) {
            if (nuevoEstado == EstadoPedido.CANCELADO || nuevoEstado == EstadoPedido.ENTREGADO) {
                pedido.setEstado(nuevoEstado);
            } else {
                throw new IllegalArgumentException("El comprador no puede cambiar a este estado");
            }
        }

        // Si se rechaza o cancela, devolver el stock
        if (nuevoEstado == EstadoPedido.RECHAZADO || nuevoEstado == EstadoPedido.CANCELADO) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto p = detalle.getProducto();
                p.setStock(p.getStock() + detalle.getCantidad());
                p.setActivo(true);
                productoRepository.save(p);
            }
        }

        pedidoRepository.save(pedido);
    }
}
