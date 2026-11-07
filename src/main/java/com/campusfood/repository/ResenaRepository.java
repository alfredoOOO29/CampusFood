package com.campusfood.repository;

import com.campusfood.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByProductoIdOrderByFechaCreacionDesc(Long productoId);
    boolean existsByProductoIdAndUsuarioId(Long productoId, Long usuarioId);

    @Query("SELECT AVG(r.estrellas) FROM Resena r WHERE r.producto.vendedor.id = :vendedorId")
    Double findAverageRatingByVendedorId(@Param("vendedorId") Long vendedorId);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.producto.vendedor.id = :vendedorId")
    Long countByVendedorId(@Param("vendedorId") Long vendedorId);
}
