package com.campusfood.repository;

import com.campusfood.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByVendedorIdOrderByFechaCreacionDesc(Long vendedorId);

    // Catálogo General
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock > 0 ORDER BY p.fechaCreacion DESC")
    List<Producto> findAllDisponibles();

    // Búsqueda simple por nombre o descripción
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock > 0 AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.categoria.nombre) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY p.fechaCreacion DESC")
    List<Producto> searchDisponibles(@Param("q") String query);

    // Filtrar por categoría
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock > 0 AND p.categoria.id = :catId ORDER BY p.fechaCreacion DESC")
    List<Producto> findByCategoriaDisponibles(@Param("catId") Long categoriaId);

    // Disponible Ahora
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock > 0 AND p.disponibilidad.fechaDisponible = :fecha AND p.disponibilidad.horaInicio <= :hora AND p.disponibilidad.horaFin >= :hora ORDER BY p.fechaCreacion DESC")
    List<Producto> findDisponiblesAhora(@Param("fecha") LocalDate fecha, @Param("hora") LocalTime hora);
}
