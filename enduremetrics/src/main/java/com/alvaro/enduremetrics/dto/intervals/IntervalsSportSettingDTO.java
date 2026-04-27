package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntervalsSportSettingDTO(

        @JsonProperty("id")
        Long id,
        List<String> types,
        Integer ftp,
        //Mapeamos el umbral de lactato
        @JsonProperty("lthr")
        Integer umbralLactato,
        //Mapeamos la frecuencia cardíaca
        @JsonProperty("max_hr")
        Integer fcm


) {
}
