package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.MetricaCorporal;
import com.alvaro.enduremetrics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetricaCorporalRepository extends JpaRepository<MetricaCorporal, Long> {

    List<MetricaCorporal> findByUsuarioOrderByFechasAsc(Usuario usuario);

}