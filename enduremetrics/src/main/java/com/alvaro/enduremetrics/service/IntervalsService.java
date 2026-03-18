package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.dto.intervals.IntervalsAthleteDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsUpdateProfileDTO;
import com.alvaro.enduremetrics.entity.IntervalsCredentials;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.IntervalsRepository;
import com.alvaro.enduremetrics.repository.UsuarioRepository;
import com.alvaro.enduremetrics.session.UserSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
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


    @Transactional
    public void sincronizarDatosAtleta(Usuario usuarioSession, IntervalsAthleteDTO atletaDTO) {
        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername()).orElseThrow(()
                -> new RuntimeException("Usuario no encontrado en la base de datos"));

        boolean hayCambios = false;

        if (atletaDTO.peso() != null && atletaDTO.peso() > 0) {
            usuarioGestionado.setPeso(atletaDTO.peso());
            hayCambios = true;
            System.out.println("Sincronizando peso desde Intervals: " + atletaDTO.peso());
        }

        if (hayCambios) {
            usuarioRepository.save(usuarioGestionado);
            userSession.setUsuarioLogueado(usuarioGestionado);
        }
    }


    @Async
    @Transactional
    public void sincronizacionBackground(Usuario usuarioSession) {

        try {
            System.out.println("Iniciando sincronización en segundo plano");

            obtenerCredenciales(usuarioSession).ifPresent(creds -> {
                IntervalsAthleteDTO atleta = probarConexion(usuarioSession);
                sincronizarDatosAtleta(usuarioSession, atleta);
                System.out.println("Sincronización completada con éxito");
            });
        } catch (Exception e) {
            System.out.println("Error al sincronizar en segundo plano: " + e.getMessage());
        }

    }


    @Async
    public void actualizarPerfilIntervals(Usuario usuarioSession, Double nuevoPeso, Double altura, LocalDate fechaNacimiento, String sexoUi) {
        try {
            obtenerCredenciales(usuarioSession).ifPresent(creds -> {
                String authRaw = "API_KEY:" + creds.apiKey();
                String encodedAuth = Base64.getEncoder().encodeToString(authRaw.getBytes());

                // 1. Convertimos centímetros a metros
                Double alturaApi = (altura != null) ? altura / 100.0 : null;

                // 2. Traducimos el idioma
                String sexoApi = null;
                if ("Hombre".equals(sexoUi)) sexoApi = "M";
                else if ("Mujer".equals(sexoUi)) sexoApi = "F";

                // 3. Pasamos las variables CORRECTAS (alturaApi y sexoApi)
                IntervalsUpdateProfileDTO updateDTO = new IntervalsUpdateProfileDTO(nuevoPeso, alturaApi, fechaNacimiento, sexoApi);

                System.out.println("[PUSH] Subiendo nuevo perfil a Intervals...");

                restClient.put()
                        .uri("/athlete/{id}", creds.athleteId())
                        .header("Authorization", "Basic " + encodedAuth)
                        .body(updateDTO)
                        .retrieve()
                        .toBodilessEntity();

                System.out.println("[PUSH] Perfil actualizado correctamente en la red.");
            });
        } catch (Exception e) {
            System.err.println("[PUSH ERROR] No se pudo actualizar el perfil en Intervals: " + e.getMessage());
        }
    }

}