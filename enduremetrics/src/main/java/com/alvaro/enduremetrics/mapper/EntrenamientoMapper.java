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
                carrera.setOscilacionVertical(dto.oscilacionVertical());
                carrera.setRatioVertical(dto.ratioVertical());

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

}
