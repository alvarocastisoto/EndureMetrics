package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, Long> {
    boolean existsByIntervalsId(String intervalsId);

    List<Entrenamiento> findByUsuarioOrderByFechaInicioDesc(Usuario usuario);

    List<Entrenamiento> findByUsuarioAndFechaInicioBetween(
            Usuario usuario,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Entrenamiento> findByUsuarioAndTipoAndFechaAfter(Usuario usuario, String tipo, LocalDate fechaLimite);
}