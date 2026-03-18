package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.dto.intervals.IntervalsAthleteDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsDTO;
import com.alvaro.enduremetrics.entity.IntervalsCredentials;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.IntervalsRepository;
import com.alvaro.enduremetrics.repository.UsuarioRepository;
import com.alvaro.enduremetrics.session.UserSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Base64;

@Service
public class IntervalsService {

    private final IntervalsRepository credentialsRepository;
    private final UsuarioRepository usuarioRepository; // <-- Añadido de nuevo
    private final RestClient restClient;
    private final UserSession userSession;

    public IntervalsService(IntervalsRepository credentialsRepository, UsuarioRepository usuarioRepository, UserSession userSession) {
        this.credentialsRepository = credentialsRepository;
        this.usuarioRepository = usuarioRepository;
        this.userSession = userSession;
        this.restClient = RestClient.builder()
                .baseUrl("https://intervals.icu/api/v1")
                .build();
    }

    @Transactional // Vital para asegurar que toda la operación ocurre en una sola conexión a BD
    public void agregarInvervals(Usuario usuarioSession, String athleteId, String apiKey) {
        if (athleteId != null && !athleteId.isBlank() && apiKey != null && !apiKey.isBlank()) {

            // 1. Recargamos el usuario para que sea un objeto "gestionado" (Managed) por Hibernate
            Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

            // 2. Ahora usamos el usuarioGestionado en lugar del usuario de la sesión
            IntervalsCredentials credenciales = credentialsRepository.findByUsuario(usuarioGestionado)
                    .orElse(new IntervalsCredentials());

            credenciales.setUsuario(usuarioGestionado);
            credenciales.setIntervalsId(athleteId);
            credenciales.setIntervalsApiKey(apiKey);

            credentialsRepository.save(credenciales);

        } else {
            throw new IllegalArgumentException("El API Key y el ID del atleta son obligatorios.");
        }
    }

    @Transactional(readOnly = true)
    public IntervalsAthleteDTO probarConexion(Usuario usuarioSession) {

        // 1. Recargamos también el usuario aquí para evitar fallos en la consulta SELECT
        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

        IntervalsCredentials creds = credentialsRepository.findByUsuario(usuarioGestionado)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene Intervals vinculado."));

        String authRaw = "API_KEY:" + creds.getIntervalsApiKey();
        String encodedAuth = Base64.getEncoder().encodeToString(authRaw.getBytes());

        try {
            return restClient.get()
                    .uri("/athlete/{id}", creds.getIntervalsId())
                    .header("Authorization", "Basic " + encodedAuth)
                    .retrieve()
                    .body(IntervalsAthleteDTO.class);

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalArgumentException("Credenciales rechazadas por Intervals. Revisa el ID y la API Key.");
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con Intervals: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public java.util.Optional<IntervalsDTO> obtenerCredenciales(Usuario usuarioSession) {

        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

        // En lugar de lanzar excepción, devolvemos un Optional. Si hay credenciales, devuelve el DTO. Si no, devuelve vacío.
        return credentialsRepository.findByUsuario(usuarioGestionado)
                .map(creds -> new IntervalsDTO(creds.getIntervalsId(), creds.getIntervalsApiKey()));
    }
}