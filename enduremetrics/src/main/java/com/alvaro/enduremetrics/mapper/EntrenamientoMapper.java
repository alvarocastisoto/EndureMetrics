package com.alvaro.enduremetrics.mapper;

import com.alvaro.enduremetrics.dto.intervals.IntervalsActivityDTO;
import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoGimnasio;
import org.springframework.stereotype.Component;

@Component
public class EntrenamientoMapper {
    public Entrenamiento toEntity(IntervalsActivityDTO dto) {
        if (dto == null) {
            return null;
        }

        Entrenamiento entreno;

        String tipoSeguro = dto.getType() != null ? dto.getType().toLowerCase() : "desconocido";

        switch (tipoSeguro) {
            case "run":
                EntrenamientoCarrera carrera = new EntrenamientoCarrera();
                carrera.setDesnivelPositivo(dto.getDesnivelPositivo());
                carrera.setRitmoMedio(dto.getRitmoMedio());
                carrera.setGapRitmoAjustado(dto.getGapRitmoAjustado());
                carrera.setPotenciaCarreraMedia(dto.getPotenciaMedia());
                carrera.setCadenciaMedia(dto.getCadenciaMedia());
                carrera.setLongitudZancada(dto.getLongitudZancada());
                carrera.setTiempoContactoSuelo(dto.getTiempoContactoSuelo());
                carrera.setOscilacionVertical(dto.getOscilacionVertical());
                carrera.setRatioVertical(dto.getRatioVertical());

                entreno = carrera;
                break;
            case "weighttraining":
            case "workout":
                EntrenamientoGimnasio gym = new EntrenamientoGimnasio();
                gym.setVolumenTotalKg(dto.getVolumenTotalKg());

                entreno = gym;
                break;

            default:
                // Fallback: Si manda "yoga", "swim" o "desconocido", no perdemos el TSS
                entreno = new Entrenamiento();
        }
        entreno.setIntervalsId(dto.getId());
        entreno.setFechaInicio(dto.getFechaInicio());
        entreno.setDistancia(dto.getDistancia());
        entreno.setTiempoMovimiento(dto.getTiempoMovimiento());
        entreno.setCargaTss(dto.getCargaTss());
        entreno.setFrecuenciaCardiacaMedia(dto.getFrecuenciaCardiacaMedia());
        entreno.setCalorias(dto.getCalorias());


        return entreno;
    }

}
