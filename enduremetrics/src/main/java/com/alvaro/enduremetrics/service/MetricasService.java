package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.repository.EntrenamientoCarreraRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
            Double vo2MaxEstimado = calcularVo2MaxIndividual(carrera, usuario);

            if (vo2MaxEstimado != null) {
                sumaVo2Max += vo2MaxEstimado;
                entrenosValidos++;
            }
        }

        return entrenosValidos > 0 ? sumaVo2Max / entrenosValidos : null;
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


    public Map<LocalDate, Double> obtenerHistoricoVo2Max(Usuario usuario) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0);
        List<EntrenamientoCarrera> carreras = carreraRepository.buscarCarrerasRecientes(usuario, fechaLimite);

        // 1. TRAMPA TEMPORAL: Le damos la vuelta a la lista para procesar de más antigua a más reciente
        Collections.reverse(carreras);

        Map<LocalDate, Double> historico = new TreeMap<>();

        // 2. Variables para la Media Móvil Exponencial (EMA)
        Double emaAnterior = null;
        double alpha = 0.25; // Le damos un 25% de importancia al entreno de hoy y 75% al historial

        for (EntrenamientoCarrera carrera : carreras) {
            Double vo2Bruto = calcularVo2MaxIndividual(carrera, usuario);

            if (vo2Bruto != null) {
                // 3. Aplicamos la lógica EMA
                if (emaAnterior == null) {
                    // Si es la primera carrera válida del mes, no hay historial. El punto de partida es el valor bruto.
                    emaAnterior = vo2Bruto;
                } else {
                    // Fórmula: (Hoy * 0.25) + (Ayer * 0.75)
                    emaAnterior = (vo2Bruto * alpha) + (emaAnterior * (1.0 - alpha));
                }

                // 4. Guardamos el valor SUAVIZADO, no el bruto
                historico.put(carrera.getFechaInicio().toLocalDate(), emaAnterior);
            }
        }

        return historico;
    }


    public Double calcularCargaSemanalTRIMP(Usuario usuario) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0);
        List<EntrenamientoCarrera> carreras = carreraRepository.buscarCarrerasRecientes(usuario, fechaLimite);

        // Escudo inicial
        if (usuario.getFcMax() == null || usuario.getFcReposo() == null) {
            return 0.0;
        }

        double trimpTotalSemana = 0.0;

        for (EntrenamientoCarrera carrera : carreras) {
            Integer fcMedia = carrera.getFrecuenciaCardiacaMedia();
            Integer tiempoSegundos = carrera.getTiempoMovimiento();

            // 1. Filtro seguro: Usamos continue para no abortar toda la semana si un día falla
            if (tiempoSegundos == null || fcMedia == null || fcMedia <= usuario.getFcReposo()) {
                continue;
            }

            // 2. Pasamos a minutos
            double t = tiempoSegundos / 60.0;

            // 3. RFC como Double y con paréntesis correctos
            double rfc = (double) (fcMedia - usuario.getFcReposo()) / (usuario.getFcMax() - usuario.getFcReposo());

            // 4. Fórmula TRIMP de Banister
            double trimpSesion;
            // Comprobamos mujer explícitamente, por defecto aplicamos la constante de hombre (1.92)
            if (usuario.getSexo() != null && usuario.getSexo().equalsIgnoreCase("mujer")) {
                trimpSesion = t * rfc * 0.64 * Math.exp(1.67 * rfc);
            } else {
                trimpSesion = t * rfc * 0.64 * Math.exp(1.92 * rfc);
            }

            // Sumamos a la mochila de la semana
            trimpTotalSemana += trimpSesion;
        }

        return trimpTotalSemana;
    }

    public Double calcularTsb(Usuario usuario) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(42).withHour(0).withMinute(0);
        List<EntrenamientoCarrera> carreras = carreraRepository.buscarCarrerasRecientes(usuario, fechaLimite);

        // Escudo inicial
        if (usuario.getFcMax() == null || usuario.getFcReposo() == null) {
            return 0.0;
        }

        // =========================================================
        // FASE 1: Construir el mapa de Carga por Día (Memoria RAM)
        // =========================================================
        Map<LocalDate, Double> trimpPorDia = new TreeMap<>();

        for (EntrenamientoCarrera carrera : carreras) {
            Integer fcMedia = carrera.getFrecuenciaCardiacaMedia();
            Integer tiempoSegundos = carrera.getTiempoMovimiento();

            if (tiempoSegundos == null || fcMedia == null || fcMedia <= usuario.getFcReposo()) {
                continue;
            }

            double t = tiempoSegundos / 60.0;
            double rfc = (double) (fcMedia - usuario.getFcReposo()) / (usuario.getFcMax() - usuario.getFcReposo());

            double trimpSesion;
            if (usuario.getSexo() != null && usuario.getSexo().equalsIgnoreCase("mujer")) {
                trimpSesion = t * rfc * 0.64 * Math.exp(1.67 * rfc);
            } else {
                trimpSesion = t * rfc * 0.64 * Math.exp(1.92 * rfc);
            }

            LocalDate fechaCarrera = carrera.getFechaInicio().toLocalDate();

            trimpPorDia.merge(fechaCarrera, trimpSesion, Double::sum);
        }

        Double ctlAyer = 0.0;
        Double atlAyer = 0.0;
        Double ctlHoy = 0.0;
        Double atlHoy = 0.0;

        // Viajamos desde hace 42 días hasta hoy (0)
        for (int i = 42; i >= 0; i--) {
            LocalDate fechaActual = LocalDate.now().minusDays(i);

            Double trimpDelDia = trimpPorDia.getOrDefault(fechaActual, 0.0);

            // Las matemáticas de desgaste siempre aplican, descanses o corras
            ctlHoy = (trimpDelDia * (1.0 / 42.0)) + (ctlAyer * (1.0 - (1.0 / 42.0)));
            atlHoy = (trimpDelDia * (1.0 / 7.0)) + (atlAyer * (1.0 - (1.0 / 7.0)));

            // Preparamos los datos para la vuelta de mañana
            ctlAyer = ctlHoy;
            atlAyer = atlHoy;
        }

        // Al salir del bucle, ctlHoy y atlHoy tienen exactamente los valores de HOY
        return ctlHoy - atlHoy;
    }
    private Double calcularVo2MaxIndividual(EntrenamientoCarrera carrera, Usuario usuario) {

        Integer fcMedia = carrera.getFrecuenciaCardiacaMedia();

        // 1. Pasa los segundos a minutos (ya lo tienes de antes)

        double tiempoMovimientoMinutos = carrera.getTiempoMovimiento() / 60.0;

        // 2. El IF del filtro: ¿Es nula la FC? ¿Es menor que el reposo? ¿Dura menos de 15 min?
        if (fcMedia == null || fcMedia <= usuario.getFcReposo() || tiempoMovimientoMinutos < 15) {
            return null; // Si es basura, devolvemos nulo y listos
        }

        // 3. Matemática: porcentajeRfc
        double porcentajeRfc = (double) (fcMedia - usuario.getFcReposo()) / (usuario.getFcMax() - usuario.getFcReposo());

        // 4. Matemática: metrosPorMinuto
        double metrosPorMinuto = carrera.getDistancia() / tiempoMovimientoMinutos;

        // 5. Matemática: vo2Actual y vo2MaxEstimado
        double vo2Actual = (0.2 * metrosPorMinuto) + 3.5;
        double vo2MaxEstimado = vo2Actual / porcentajeRfc;
        // 6. El filtro final de realismo humano
        if (vo2MaxEstimado > 20 && vo2MaxEstimado < 95) {
            return vo2MaxEstimado; // ¡Carrera válida! Devolvemos el cálculo
        }

        return null; // Si sale un número marciano, devolvemos nulo
    }
}




