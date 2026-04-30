package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.repository.EntrenamientoCarreraRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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


    public Map<String, String> calcularZonasKarvonen(Usuario usuario) {
        Integer fcMax = usuario.getFcMax();
        Integer fcReposo = usuario.getFcReposo();
        if (fcMax == null || fcReposo == null) {
            return null;
        }
        // Rango de latidos útiles que tiene el atleta
        Integer rfc = fcMax - fcReposo;

        Map<String, String> zonas = new LinkedHashMap<>();

        double[][] rangos = {
                {0.50, 0.60}, // Z1
                {0.60, 0.70}, // Z2
                {0.70, 0.80}, // Z3
                {0.80, 0.90}, // Z4
                {0.90, 1.00}  // Z5
        };

        for (int i = 0; i < rangos.length; i++) {
            int min = (int) Math.round((rfc * rangos[i][0] + fcReposo));
            int max = (int) Math.round((rfc * rangos[i][1] + fcReposo));
            zonas.put("Z" + (i + 1), min + " - " + max + " ppm");
        }

        return zonas;
    }

    public Map<LocalDate, Double> obtenerHistoricoVo2Max(Usuario usuario){
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0);
        List<EntrenamientoCarrera> carreras = carreraRepository.buscarCarrerasRecientes(usuario, fechaLimite);

        Map<LocalDate, Double> historico = new TreeMap<>(); // TreeMap para que las fechas salgan ordenadas

        for (EntrenamientoCarrera carrera : carreras) {
            Double vo2 = calcularVo2MaxIndividual(carrera, usuario);
            if (vo2 != null) {
                historico.put(carrera.getFechaInicio().toLocalDate(), vo2);
            }
        }
        return historico;
    }

    private Double calcularVo2MaxIndividual(EntrenamientoCarrera carrera, Usuario usuario){
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0);

    }

}


