package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// Sirve para ignorar lo que no solicitamos en el dto
@JsonIgnoreProperties(ignoreUnknown = true)
public record IntervalsActivityDTO(
        String id,
        String type,
        @JsonProperty("start_date_local")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaInicio,
        Double distance,
        @JsonProperty("moving_time")
        Integer tiempoMovimiento

) {
}
