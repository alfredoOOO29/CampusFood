package com.campusfood.repository;

import com.campusfood.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCompradorIdOrderByFechaPedidoDesc(Long compradorId);
    List<Pedido> findByVendedorIdOrderByFechaPedidoDesc(Long vendedorId);
}
