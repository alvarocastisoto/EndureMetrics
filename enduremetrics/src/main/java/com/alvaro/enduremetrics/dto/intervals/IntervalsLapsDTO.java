package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntervalsLapsDTO(

        @JsonProperty("type") String tipoPaso,
        @JsonProperty("count") Integer numeroVuelta,

        // Tiempo y Distancia
        @JsonProperty("distance") Double distanciaMetros,
        @JsonProperty("elapsed_time")
        @JsonAlias({"time"})
        Integer tiempoSegundos,

        @JsonProperty("moving_time") Integer tiempoMovimientoSegundos,

        // Ritmos y Biomecánica Básica
        @JsonProperty("average_speed") Double ritmoMedio,
        @JsonProperty("max_speed") @JsonAlias({"speed_max", "top_speed"}) Double ritmoOptimo,
        @JsonProperty("gap")
        @JsonAlias({"average_gap"})
        Double gapRitmoAjustado,
        @JsonProperty("min_heartrate") Integer frecuenciaCardiacaMinima,
        @JsonProperty("average_heartrate") Integer frecuenciaCardiacaMedia,
        @JsonProperty("max_heartrate") Integer frecuenciaCardiacaMaxima,
        @JsonProperty("average_cadence") Integer cadenciaMedia,
        @JsonProperty("max_cadence") Integer cadenciaMaxima,

        // Topografía
        @JsonProperty("total_elevation_gain") Integer ascensoTotal,

        // CORRECCIÓN: Potencia y Dinámica con alias protectores
        @JsonProperty("average_watts") Integer potenciaMedia,

        @JsonProperty("normalized_power")
        @JsonAlias({"normalized_watts", "np"})
        Integer potenciaNormalizada,

        @JsonProperty("average_stride")
        @JsonAlias({"stride_length", "stride"})
        Double longitudZancadaMedia,
        @JsonProperty("calories") Integer calorias,

        @JsonProperty("average_ground_contact_time")
        @JsonAlias({"ground_contact_time", "gct"})
        Integer tiempoContactoSuelo,

        @JsonProperty("average_vertical_oscillation")
        @JsonAlias({"vertical_oscillation", "AvgVerticalOscillation"})
        Double oscilacionVertical,

        @JsonProperty("avg_lr_balance")
        @JsonAlias({"ground_contact_balance", "gct_balance", "average_gct_balance", "GarminGCTBalance"})
        Double equilibrioTcsIzquierda,
        @JsonProperty("vertical_ratio") Double relacionVertical,

        @JsonProperty("average_temp") Double temperaturaMedia
) {
}