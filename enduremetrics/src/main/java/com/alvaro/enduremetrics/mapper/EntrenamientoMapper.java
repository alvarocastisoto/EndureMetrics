package com.alvaro.enduremetrics.mapper;

import com.alvaro.enduremetrics.dto.intervals.IntervalsActivityDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsLapsDTO;
import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoGimnasio;
import com.alvaro.enduremetrics.entity.entrenamiento.VueltaCarrera;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntrenamientoMapper {
    public Entrenamiento toEntity(IntervalsActivityDTO dto) {
        if (dto == null) {
            return null;
        }

        Entrenamiento entreno;

        String tipoSeguro = dto.type() != null ? dto.type().toLowerCase() : "desconocido";

        switch (tipoSeguro) {
            case "run":
                EntrenamientoCarrera carrera = new EntrenamientoCarrera();
                carrera.setDesnivelPositivo(dto.desnivelPositivo());
                carrera.setRitmoMedio(dto.ritmoMedio());
                carrera.setGapRitmoAjustado(dto.gapRitmoAjustado());
                carrera.setPotenciaCarreraMedia(dto.potenciaMedia());
                carrera.setCadenciaMedia(dto.cadenciaMedia());
                carrera.setLongitudZancada(dto.longitudZancada());
                carrera.setTiempoContactoSuelo(dto.tiempoContactoSuelo());
                if (dto.oscilacionVertical() != null) {
                    carrera.setOscilacionVertical(dto.oscilacionVertical() / 10.0);
                }
                carrera.setRatioVertical(dto.ratioVertical());

                carrera.setDesacopleAerobico(dto.desacopleAerobico());

                carrera.setVueltas(mapearVueltas(dto.vueltas(), carrera));
                entreno = carrera;
                break;
            case "weighttraining":
            case "workout":
                EntrenamientoGimnasio gym = new EntrenamientoGimnasio();
                gym.setVolumenTotalKg(dto.volumenTotalKg());

                entreno = gym;
                break;

            default:
                // Fallback: Si manda "yoga", "swim" o "desconocido", no perdemos el TSS
                entreno = new Entrenamiento();
        }
        entreno.setIntervalsId(dto.id());
        entreno.setFechaInicio(dto.fechaInicio());
        entreno.setDistancia(dto.distancia());
        entreno.setTiempoMovimiento(dto.tiempoMovimiento());
        entreno.setCargaTss(dto.cargaTss());
        entreno.setFrecuenciaCardiacaMedia(dto.frecuenciaCardiacaMedia());
        entreno.setCalorias(dto.calorias());

        return entreno;
    }

    public List<VueltaCarrera> mapearVueltas(List<IntervalsLapsDTO> lapsDto, EntrenamientoCarrera carreraBase) {
        if (lapsDto == null || lapsDto.isEmpty()) return new ArrayList<>();

        return lapsDto.stream().map(dto -> {
            VueltaCarrera v = new VueltaCarrera();
            // ¡VITAL! Establecer la relación bidireccional para que Hibernate no pete
            v.setEntrenamiento(carreraBase);
            v.setCalorias(dto.calorias());
            v.setTipoPaso(dto.tipoPaso());
            v.setNumeroVuelta(dto.numeroVuelta());
            v.setDistanciaMetros(dto.distanciaMetros());
            v.setTiempoSegundos(dto.tiempoSegundos());
            v.setTiempoMovimientoSegundos(dto.tiempoMovimientoSegundos());
            v.setRitmoMedio(dto.ritmoMedio());
            v.setRitmoOptimo(dto.ritmoOptimo());
            v.setGapMedio(dto.gapRitmoAjustado());
            v.setFrecuenciaCardiacaMinima(dto.frecuenciaCardiacaMinima());
            v.setFrecuenciaCardiacaMedia(dto.frecuenciaCardiacaMedia());
            v.setFrecuenciaCardiacaMaxima(dto.frecuenciaCardiacaMaxima());
            if (dto.cadenciaMedia() != null) v.setCadenciaMedia(dto.cadenciaMedia() * 2);
            if (dto.cadenciaMaxima() != null) v.setCadenciaMaxima(dto.cadenciaMaxima() * 2);
            v.setAscensoTotal(dto.ascensoTotal());
            v.setPotenciaMedia(dto.potenciaMedia());
            v.setPotenciaNormalizada(dto.potenciaNormalizada());
            v.setLongitudZancadaMedia(dto.longitudZancadaMedia());
            if (dto.equilibrioTcsIzquierda() != null) {
                double izq = dto.equilibrioTcsIzquierda();
                v.setEquilibrioTcsIzquierda(izq);
            }
            v.setTiempoContactoSuelo(dto.tiempoContactoSuelo());

            v.setEquilibrioTcsIzquierda(dto.equilibrioTcsIzquierda());
            if (dto.oscilacionVertical() != null) {
                v.setOscilacionVertical(dto.oscilacionVertical() / 10.0);
            }
            // --- RATIO VERTICAL % (El KPI de eficiencia) ---
            if (dto.relacionVertical() != null) {
                v.setRelacionVertical(dto.relacionVertical());
            } else if (dto.oscilacionVertical() != null && dto.longitudZancadaMedia() != null && dto.longitudZancadaMedia() > 0) {
                // Fórmula: (Oscilacion_m / Zancada_m) * 100
                double voMetros = dto.oscilacionVertical() / 1000.0;
                double ratio = (voMetros / dto.longitudZancadaMedia()) * 100;
                v.setRelacionVertical(ratio);
            }
            v.setTemperaturaMedia(dto.temperaturaMedia());
            return v;
        }).toList();
    }
}