package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.repository.EntrenamientoCarreraRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MetricasService {

    private final EntrenamientoCarreraRepository carreraRepository;

    public MetricasService(EntrenamientoCarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    public Double calcularVo2MaxReciente(Usuario usuario) {
        LocalDate fechaLimite = LocalDate.now().minusDays(21);

        List<EntrenamientoCarrera> carreras = carreraRepository.buscarCarrerasRecientes(usuario, fechaLimite);

        if (carreras.size() < 3 || usuario.getFcMax() == null || usuario.getFcReposo() == null) {
            // Un VO2Max basado en 1 o 2 carreras no es estadísticamente fiable
            return null; // <-- Corregido
        }

        double sumaVo2Max = 0.0;
        int entrenosValidos = 0;

        for (EntrenamientoCarrera carrera : carreras) {

            Integer fcMedia = carrera.getFrecuenciaCardiacaMedia();

            if (fcMedia == null || fcMedia <= usuario.getFcReposo() || carrera.getTiempoMovimiento() < 15) {
                continue;
            }

            double porcentajeRfc = (double) (fcMedia - usuario.getFcReposo()) / (usuario.getFcMax() - usuario.getFcReposo());

            double metrosPorMinuto = carrera.getDistancia() / carrera.getTiempoMovimiento();

            double vo2Actual = (0.2 * metrosPorMinuto) + 3.5;

            double vo2MaxEstimado = vo2Actual / porcentajeRfc;

            if (vo2MaxEstimado > 20 && vo2MaxEstimado < 95) {
                sumaVo2Max += vo2MaxEstimado;
                entrenosValidos++; // <-- Corregido
            }
        }

        if (entrenosValidos == 0) {
            return null;
        }

        return sumaVo2Max / entrenosValidos;
    }}


