package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Sirve para ignorar lo que no solicitamos en el dto
@JsonIgnoreProperties(ignoreUnknown = true)
public record IntervalsActivityDTO(
        String id,
        String type,
        @JsonProperty("start_date_local")
        String fechaInicio,
        Double distance,
        @JsonProperty("moving_time")
        Integer tiempoMovimiento

) {
}
