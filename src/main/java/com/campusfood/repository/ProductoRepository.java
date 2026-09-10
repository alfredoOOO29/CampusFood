package com.campusfood.repository;

import com.campusfood.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByVendedorIdOrderByFechaCreacionDesc(Long vendedorId);
}
