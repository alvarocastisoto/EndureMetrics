package com.alvaro.enduremetrics.entity.entrenamiento;

import com.alvaro.enduremetrics.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entrenamientos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_deporte", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public class Entrenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(unique = true)
    private String intervalsId;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "distancia", nullable = true) // <--- Forzamos que sea nullable
    private Double distancia;

    @Column(name = "tiempo_movimiento", nullable = true)
    private Integer tiempoMovimiento;

    @Column(name = "carga_tss")
    private Integer cargaTss; // Training Stress Score

    @Column(name = "frecuencia_cardiaca_media")
    private Integer frecuenciaCardiacaMedia;

    @Column(name = "calorias")
    private Integer calorias;

}
