package com.alvaro.enduremetrics.entity.entrenamiento;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Ride") // Cuando guardes esta clase, Hibernate pondrá "Ride" en la columna tipo_deporte
@Getter
@Setter
@NoArgsConstructor
public class EntrenamientoCiclismo extends Entrenamiento {
    // Aquí en el futuro pondremos watios medios, FTP de la sesión, etc.
}