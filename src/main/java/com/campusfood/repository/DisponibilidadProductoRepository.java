package com.campusfood.repository;

import com.campusfood.entity.DisponibilidadProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisponibilidadProductoRepository extends JpaRepository<DisponibilidadProducto, Long> {
}
