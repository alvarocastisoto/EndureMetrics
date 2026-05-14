package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.MetricaCorporal;
import com.alvaro.enduremetrics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetricaCorporalRepository extends JpaRepository<MetricaCorporal, Long> {

    List<MetricaCorporal> findByUsuarioOrderByFechaAsc(Usuario usuario);
    List<MetricaCorporal> findByUsuarioAndFechaAfterOrderByFechaAsc(Usuario usuario, LocalDate fechaLimite);
    Optional<MetricaCorporal> findByUsuarioAndFecha(Usuario usuario, LocalDate fechaActual);
}
