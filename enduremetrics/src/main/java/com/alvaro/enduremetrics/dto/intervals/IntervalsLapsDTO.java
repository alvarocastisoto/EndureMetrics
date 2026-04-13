package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntervalsLapsDTO(

        @JsonProperty("type") String tipoPaso, // "Warmup", "Work", "Recovery", "Active"
        @JsonProperty("count") Integer numeroVuelta,

        // Tiempo y Distancia
        @JsonProperty("distance") Double distanciaMetros,
        @JsonProperty("time") Integer tiempoSegundos,
        @JsonProperty("moving_time") Integer tiempoMovimientoSegundos,

        // Ritmos y Biomecánica Básica
        @JsonProperty("average_speed") Double ritmoMedio,
        @JsonProperty("average_heartrate") Integer frecuenciaCardiacaMedia,
        @JsonProperty("max_heartrate") Integer frecuenciaCardiacaMaxima,
        @JsonProperty("average_cadence") Integer cadenciaMedia,

        // Topografía
        @JsonProperty("total_elevation_gain") Integer ascensoTotal,
        @JsonProperty("total_elevation_loss") Integer descensoTotal,

        // Potencia y Dinámica Avanzada (Pueden venir nulos según el reloj)
        @JsonProperty("average_watts") Integer potenciaMedia,
        @JsonProperty("normalized_watts") Integer potenciaNormalizada,
        @JsonProperty("stride_length") Double longitudZancadaMedia,
        @JsonProperty("ground_contact_time") Integer tiempoContactoSuelo,
        @JsonProperty("vertical_oscillation") Double oscilacionVertical,
        @JsonProperty("vertical_ratio") Double relacionVertical
) {}