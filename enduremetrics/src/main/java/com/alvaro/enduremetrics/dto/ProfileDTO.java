package com.alvaro.enduremetrics.dto;

import java.time.LocalDate;

public record ProfileDTO (String username, Integer altura, LocalDate fechaNacimiento, String sexo, Double peso){
}
