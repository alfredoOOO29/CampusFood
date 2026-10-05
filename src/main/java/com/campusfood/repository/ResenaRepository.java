package com.campusfood.repository;

import com.campusfood.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByProductoIdOrderByFechaCreacionDesc(Long productoId);
    boolean existsByProductoIdAndUsuarioId(Long productoId, Long usuarioId);
}
