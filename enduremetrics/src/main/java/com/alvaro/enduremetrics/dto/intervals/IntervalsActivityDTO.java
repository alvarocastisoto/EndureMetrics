package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // <-- BLINDAJE SENIOR
public class IntervalsActivityDTO {

    // --- CAMPOS COMUNES / IDENTIFICACIÓN ---

    @JsonProperty("id")
    private String id; // El ID único de Intervals

    @JsonProperty("type")
    private String type; // Ej: "Run", "Ride", "WeightTraining"

    @JsonProperty("start_date_local")
    private LocalDateTime fechaInicio; // Fecha y hora local del entreno

    // --- MÉTRICAS FISIOLÓGICAS (Clase Padre) ---

    @JsonProperty("distance")
    private Double distancia; // En metros

    @JsonProperty("moving_time")
    private Integer tiempoMovimiento; // En segundos

    @JsonProperty("icu_training_load")
    private Integer cargaTss; // TSS calculado por Intervals

    @JsonProperty("average_heartrate")
    private Integer frecuenciaCardiacaMedia;

    @JsonProperty("calories")
    private Integer calorias;

    // --- MÉTRICAS DE CARRERA / BIOMECÁNICA ---

    @JsonProperty("total_elevation_gain")
    private Integer desnivelPositivo;

    @JsonProperty("average_speed")
    private Double ritmoMedio; // Intervals lo manda en m/s

    @JsonProperty("gap")
    private Double gapRitmoAjustado; // Grade Adjusted Pace

    @JsonProperty("average_watts")
    private Integer potenciaMedia; // Sirve tanto para bici como para carrera (Stryd)

    @JsonProperty("average_cadence")
    private Integer cadenciaMedia;

    // OJO: Los campos de dinámica avanzada a veces cambian o vienen nulos si el usuario no tiene banda HRM-Pro o Stryd
    @JsonProperty("stride_length")
    private Double longitudZancada;

    @JsonProperty("ground_contact_time")
    private Integer tiempoContactoSuelo;

    @JsonProperty("vertical_oscillation")
    private Double oscilacionVertical;

    @JsonProperty("vertical_ratio")
    private Double ratioVertical;

    // --- MÉTRICAS DE GIMNASIO ---

    @JsonProperty("work") // Intervals suele usar 'work' para el volumen/carga en Kj, podemos mapearlo al volumen
    private Integer volumenTotalKg;

}