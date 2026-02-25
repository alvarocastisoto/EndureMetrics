package com.alvaro.garmin.entity;

import java.time.LocalDate; // Añadido para la fecha
import java.util.ArrayList;
import java.util.List; // Añadido para que no rompa la compilación
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Datos Biométricos
    private Double peso;
    private Integer altura;
    private LocalDate fechaNacimiento;
    private String sexo;

    // --- INVENTARIO DE MATERIAL ---
    // Si borramos al usuario, se borran sus zapatillas (CascadeType.ALL)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Zapatilla> armarioZapatillas = new ArrayList<>();

    // Método helper profesional para añadir material
    public void addZapatilla(Zapatilla zapatilla) {
        armarioZapatillas.add(zapatilla);
        zapatilla.setUsuario(this);
    }
}