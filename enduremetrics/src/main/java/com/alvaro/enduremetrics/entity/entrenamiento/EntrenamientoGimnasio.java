package com.alvaro.enduremetrics.entity.entrenamiento;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("WeightTraining") // Identificador estándar para gimnasio en APIs deportivas
@Getter
@Setter
@NoArgsConstructor
public class EntrenamientoGimnasio extends Entrenamiento {

    @Column(name = "volumen_total_kg")
    private Integer volumenTotalKg; // Kilos totales levantados en la sesión (opcional)

}