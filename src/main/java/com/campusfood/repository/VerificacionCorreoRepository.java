package com.campusfood.repository;

import com.campusfood.entity.VerificacionCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificacionCorreoRepository extends JpaRepository<VerificacionCorreo, Long> {
    Optional<VerificacionCorreo> findByToken(String token);
}
