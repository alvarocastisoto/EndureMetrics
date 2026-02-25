package com.alvaro.garmin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "zapatillas")
@Getter
@Setter
public class Zapatilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    // Control de desgaste
    private Double kmActuales = 0.0;
    private Double kmMaximos = 600.0; // Valor por defecto seguro

    // Para la auto-asignación inteligente que comentamos
    private String tipoTerreno; // "Asfalto", "Trail", "Pista"

    // Si ya no se usan (jubiladas) pero queremos mantener el historial
    private boolean activa = true;

    // Relación: Muchas zapatillas pertenecen a un Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}