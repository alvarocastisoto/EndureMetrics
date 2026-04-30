package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.repository.EntrenamientoCarreraRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetricasService {

    private final EntrenamientoCarreraRepository carreraRepository;

    public MetricasService(EntrenamientoCarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    public Double calcularVo2MaxReciente(Usuario usuario) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(21).withHour(0).withMinute(0);
        List<EntrenamientoCarrera> carreras = carreraRepository.buscarCarrerasRecientes(usuario, fechaLimite);


        if (carreras.size() < 3 || usuario.getFcMax() == null || usuario.getFcReposo() == null) {
            return null;
        }

        double sumaVo2Max = 0.0;
        int entrenosValidos = 0;

        for (EntrenamientoCarrera carrera : carreras) {
            Integer fcMedia = carrera.getFrecuenciaCardiacaMedia();

            double tiempoMovimientoMinutos = carrera.getTiempoMovimiento() / 60.0;

            if (fcMedia == null || fcMedia <= usuario.getFcReposo() || tiempoMovimientoMinutos < 15) {
                continue;
            }

            double porcentajeRfc = (double) (fcMedia - usuario.getFcReposo()) / (usuario.getFcMax() - usuario.getFcReposo());

            double metrosPorMinuto = carrera.getDistancia() / tiempoMovimientoMinutos;

            double vo2Actual = (0.2 * metrosPorMinuto) + 3.5;
            double vo2MaxEstimado = vo2Actual / porcentajeRfc;

            if (vo2MaxEstimado > 20 && vo2MaxEstimado < 95) {
                sumaVo2Max += vo2MaxEstimado;
                entrenosValidos++;
            }
        }


        if (entrenosValidos == 0) {
            return null;
        }

        return sumaVo2Max / entrenosValidos;
    }

    public String estimarRitmo(Double vo2Max, int distanciaMetros) {

        if (vo2Max == null) {
            return "--:--";
        }
        Double vVo2Max = (vo2Max - 3.5) / 0.2;

        double porcentajeSostenible;
        if (distanciaMetros == 5000) {
            porcentajeSostenible = 0.95;
        } else if (distanciaMetros == 10000) {
            porcentajeSostenible = 0.90;
        } else {
            return "--:--";
        }

        Double velocidadCarrera = vVo2Max * porcentajeSostenible;

        double tiempoTotalMinutos = distanciaMetros / velocidadCarrera;

        int minutos = (int) tiempoTotalMinutos;
        int segundos = (int) Math.round((tiempoTotalMinutos - minutos) * 60);
        if (segundos == 60) {
            minutos++;
            segundos = 0;
        }

        return String.format("%02d:%02d", minutos, segundos);


    }


    public void calcularZonas(){

    }

}


