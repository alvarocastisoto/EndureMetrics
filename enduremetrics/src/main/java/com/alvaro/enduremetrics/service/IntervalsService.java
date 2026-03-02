package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.IntervalsCredentials;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.IntervalsRepository;
import org.springframework.stereotype.Service;

@Service
public class IntervalsService {

    private final IntervalsRepository intervalsRepository;

    public IntervalsService(IntervalsRepository intervalsRepository) {
        this.intervalsRepository = intervalsRepository;
    }

    public void agregarInvervals(Usuario usuario, String athleteId, String apiKey) {

        if (athleteId != null && !athleteId.isBlank() && apiKey != null && !apiKey.isBlank()) {
            //Buscamos el usuario si ya existe, si no lo creamos.
            IntervalsCredentials credenciales = intervalsRepository.findByUsuario(usuario)
                    .orElse(new IntervalsCredentials());
            //Añadimos los datos al objeto
            credenciales.setIntervalsId(athleteId);
            credenciales.setIntervalsApiKey(apiKey);
            //Guardamos las credenciales en la tabla
            intervalsRepository.save(credenciales);

        } else {
            throw new IllegalArgumentException("El API Key y el ID del atleta son obligatorios y no pueden estar vacíos.");
        }
    }
}