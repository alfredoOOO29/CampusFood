package com.campusfood.repository;

import com.campusfood.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCompradorIdOrderByFechaPedidoDesc(Long compradorId);
    List<Pedido> findByVendedorIdOrderByFechaPedidoDesc(Long vendedorId);
    
    @Query("SELECT COUNT(p) > 0 FROM Pedido p JOIN p.detalles d WHERE p.comprador.id = :compradorId AND d.producto.id = :productoId AND p.estado = 'ENTREGADO'")
    boolean hasCompradorBoughProducto(@Param("compradorId") Long compradorId, @Param("productoId") Long productoId);
}
