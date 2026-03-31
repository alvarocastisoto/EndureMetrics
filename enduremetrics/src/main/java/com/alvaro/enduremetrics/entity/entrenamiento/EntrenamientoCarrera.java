package com.alvaro.enduremetrics.entity.entrenamiento;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Run")
@Getter
@Setter
@NoArgsConstructor
public class EntrenamientoCarrera extends Entrenamiento {

    // --- TERRENO Y ESFUERZO REAL ---
    @Column(name = "desnivel_positivo")
    private Integer desnivelPositivo; // En metros

    @Column(name = "ritmo_medio")
    private Double ritmoMedio; // En m/s

    @Column(name = "gap_ritmo_ajustado")
    private Double gapRitmoAjustado; // Grade Adjusted Pace (Ritmo ajustado a la pendiente)

    @Column(name = "potencia_carrera_media")
    private Integer potenciaCarreraMedia; // Vatios al correr (Stryd/Garmin nativo)

    @Column(name = "cadencia_media")
    private Integer cadenciaMedia; // Pasos por minuto (SPM). Ideal: ~180

    @Column(name = "longitud_zancada")
    private Double longitudZancada; // En metros. Ej: 1.25m

    @Column(name = "tiempo_contacto_suelo")
    private Integer tiempoContactoSuelo; // GCT en milisegundos. Ej: 220ms

    @Column(name = "oscilacion_vertical")
    private Double oscilacionVertical; // En centímetros. Cuánto "saltas" al correr

    @Column(name = "ratio_vertical")
    private Double ratioVertical; // % (Oscilación / Longitud de zancada). Mide la eficiencia
}