package com.alvaro.enduremetrics.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class MetricaCorporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate fecha;

    @Column
    private Double peso;

    @Column
    private Double cuello;

    @Column
    private Double cintura;

    @Column
    private Double cadera;

    @Column
    private Double porcentajeGrasa;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}