package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EntrenamientoCarreraRepository extends JpaRepository<EntrenamientoCarrera, Long> {


    @Query("SELECT e FROM EntrenamientoCarrera e " +
            "WHERE e.usuario = :usuario " +
            "AND e.fecha >= :fechaDesde " +
            "ORDER BY e.fecha DESC")
    List<EntrenamientoCarrera> buscarCarrerasRecientes(
            @Param("usuario") Usuario usuario,
            @Param("fechaDesde") LocalDate fechaDesde
    );
}