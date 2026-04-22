package com.alvaro.enduremetrics.entity.entrenamiento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vueltas_carrera")
@Getter
@Setter
@NoArgsConstructor
public class VueltaCarrera {

    // --- IDENTIFICACIÓN Y RELACIÓN ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenamiento_id", nullable = false)
    private EntrenamientoCarrera entrenamiento;

    @Column(name = "numero_vuelta")
    private Integer numeroVuelta;

    @Column(name = "tipo_paso")
    private String tipoPaso; // Intervals lo llama type: "Warmup", "Active", "Recovery"

    // --- TIEMPO Y DISTANCIA ---
    @Column(name = "tiempo_segundos")
    private Integer tiempoSegundos; // El tiempo real de la vuelta

    @Column(name = "tiempo_movimiento_segundos")
    private Integer tiempoMovimientoSegundos;

    @Column(name = "tiempo_acumulado_segundos")
    private Integer tiempoAcumuladoSegundos;

    @Column(name = "distancia_metros")
    private Double distanciaMetros;

    // --- RITMOS (VELOCIDAD) ---
    @Column(name = "ritmo_medio")
    private Double ritmoMedio; // m/s (como viene de la API, luego se pasa a min/km)

    @Column(name = "ritmo_optimo")
    private Double ritmoOptimo; // El mejor ritmo en esa vuelta (Max Speed)

    @Column(name = "gap_medio")
    private Double gapMedio; // Grade Adjusted Pace (Ritmo ajustado a la pendiente)

    // --- FISIOLOGÍA CARDIACA Y METABÓLICA ---
    @Column(name = "frecuencia_cardiaca_minima")
    private Integer frecuenciaCardiacaMinima;

    @Column(name = "frecuencia_cardiaca_media")
    private Integer frecuenciaCardiacaMedia;

    @Column(name = "frecuencia_cardiaca_maxima")
    private Integer frecuenciaCardiacaMaxima;

    @Column(name = "calorias")
    private Integer calorias;

    // --- TOPOGRAFÍA ---
    @Column(name = "ascenso_total")
    private Integer ascensoTotal; // Desnivel positivo en esa vuelta


    // --- BIOMECÁNICA AVANZADA (RUNNING DYNAMICS) ---
    @Column(name = "cadencia_media")
    private Integer cadenciaMedia;

    @Column(name = "cadencia_maxima")
    private Integer cadenciaMaxima;

    @Column(name = "longitud_zancada_media")
    private Double longitudZancadaMedia; // En metros

    @Column(name = "tiempo_contacto_suelo")
    private Integer tiempoContactoSuelo; // En milisegundos

    @Column(name = "equilibrio_tcs_izquierda")
    private Double equilibrioTcsIzquierda; // Porcentaje pie izquierdo (Ej: 50.8)
    // No hace falta guardar la derecha, si la izq es 50.8, la derecha es 49.2 por matemáticas básicas.

    @Column(name = "oscilacion_vertical")

    private Double oscilacionVertical; // En centímetros

    @Column(name = "relacion_vertical")
    private Double relacionVertical; // Vertical Ratio (%)

    // --- POTENCIA DE CARRERA (STRYD / GARMIN NATIVE) ---
    @Column(name = "potencia_media")
    private Integer potenciaMedia;

    @Column(name = "potencia_maxima")
    private Integer potenciaMaxima;

    @Column(name = "potencia_normalizada")
    private Integer potenciaNormalizada; // NP®

    @Column(name = "media_w_kg")
    private Double mediaWatiosPorKg;

    @Column(name = "maximo_w_kg")
    private Double maximoWatiosPorKg;

    // --- ENTORNO ---
    @Column(name = "temperatura_media")
    private Double temperaturaMedia; // En grados Celsius
}