package com.alvaro.enduremetrics.entity.entrenamiento;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Run") // Coincide exactamente con el texto que nos devuelve el JSON de Intervals
@Getter
@Setter
@NoArgsConstructor
public class EntrenamientoCarrera extends Entrenamiento {
    // Aquí en el futuro pondremos ritmo medio, desnivel acumulado, etc.
}