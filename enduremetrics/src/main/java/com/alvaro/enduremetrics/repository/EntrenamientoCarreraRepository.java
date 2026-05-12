package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntrenamientoCarreraRepository extends JpaRepository<EntrenamientoCarrera, Long> {


    @Query("SELECT e FROM EntrenamientoCarrera e " +
            "WHERE e.usuario = :usuario " +
            "AND e.fechaInicio >= :fechaDesde " +
            "ORDER BY e.fechaInicio DESC")
    List<EntrenamientoCarrera> buscarCarrerasRecientes(
            @Param("usuario") Usuario usuario,
            @Param("fechaDesde") LocalDateTime fechaDesde
    );

    @Query("SELECT MAX(c.fechaInicio) FROM EntrenamientoCarrera c WHERE c.usuario = :usuario")
    Optional<LocalDateTime> buscarFechaUltimoEntrenamiento(@Param("usuario") Usuario usuario);


}