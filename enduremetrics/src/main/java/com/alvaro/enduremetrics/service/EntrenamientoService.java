package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.EntrenamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class EntrenamientoService {

    private final EntrenamientoRepository entrenamientoRepository;

    public EntrenamientoService(EntrenamientoRepository entrenamientoRepository) {
        this.entrenamientoRepository = entrenamientoRepository;
    }

    @Transactional(readOnly = true)
    public List<Entrenamiento> obtenerHistorial(Usuario usuario) {
        // Asumiendo que has creado este método en tu Repositorio
        // List<Entrenamiento> findByUsuarioOrderByFechaInicioDesc(Usuario usuario);
        return entrenamientoRepository.findByUsuarioOrderByFechaInicioDesc(usuario);
    }

    @Transactional(readOnly = true)
    public List<Entrenamiento> obtenerHistorialDelMes(Usuario usuario, YearMonth mes) {
        // Calculamos el primer milisegundo del mes y el último
        LocalDateTime inicioMes = mes.atDay(1).atStartOfDay();
        LocalDateTime finMes = mes.atEndOfMonth().atTime(23, 59, 59);

        return entrenamientoRepository.findByUsuarioAndFechaInicioBetween(usuario, inicioMes, finMes);
    }
}