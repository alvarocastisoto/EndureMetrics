package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // <-- BLINDAJE SENIOR
public record IntervalsActivityDTO(

        // --- CAMPOS COMUNES / IDENTIFICACIÓN ---
        @JsonProperty("id") String id,
        @JsonProperty("type") String type,
        @JsonProperty("start_date_local") LocalDateTime fechaInicio,

        // --- MÉTRICAS FISIOLÓGICAS GLOBALES ---
        @JsonProperty("distance") Double distancia,
        @JsonProperty("moving_time") Integer tiempoMovimiento,
        @JsonProperty("icu_training_load") Integer cargaTss,
        @JsonProperty("average_heartrate") Integer frecuenciaCardiacaMedia,
        @JsonProperty("calories") Integer calorias,

        // --- MÉTRICAS DE CARRERA / BIOMECÁNICA ---
        @JsonProperty("total_elevation_gain") Integer desnivelPositivo,
        @JsonProperty("average_speed") Double ritmoMedio,
        @JsonProperty("gap") Double gapRitmoAjustado,
        @JsonProperty("average_watts") Integer potenciaMedia,
        @JsonProperty("average_cadence") Integer cadenciaMedia,

        // Dinámica avanzada
        @JsonProperty("stride_length") Double longitudZancada,
        @JsonProperty("ground_contact_time") Integer tiempoContactoSuelo,
        @JsonProperty("vertical_oscillation") Double oscilacionVertical,
        @JsonProperty("vertical_ratio") Double ratioVertical,

        // --- MÉTRICAS DE GIMNASIO ---
        @JsonProperty("work") Integer volumenTotalKg,


        @JsonProperty("decoupling") Double desacopleAerobico, // Métrica clave de resistencia

        @JsonProperty("icu_intervals") List<IntervalsLapsDTO> vueltas // ARRAY DE VUELTAS
) {}