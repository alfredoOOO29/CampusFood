package com.campusfood.repository;

import com.campusfood.entity.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    List<Reclamo> findByReportadoIdOrderByFechaCreacionDesc(Long reportadoId);
    List<Reclamo> findAllByOrderByFechaCreacionDesc();
}
