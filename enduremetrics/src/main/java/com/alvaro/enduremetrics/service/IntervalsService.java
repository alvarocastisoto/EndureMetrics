package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.dto.intervals.IntervalsActivityDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsAthleteDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsDTO;
import com.alvaro.enduremetrics.dto.intervals.IntervalsUpdateProfileDTO;
import com.alvaro.enduremetrics.entity.IntervalsCredentials;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCiclismo;
import com.alvaro.enduremetrics.entity.entrenamiento.VueltaCarrera;
import com.alvaro.enduremetrics.mapper.EntrenamientoMapper;
import com.alvaro.enduremetrics.repository.EntrenamientoRepository;
import com.alvaro.enduremetrics.repository.IntervalsRepository;
import com.alvaro.enduremetrics.repository.UsuarioRepository;
import com.alvaro.enduremetrics.session.UserSession;
import org.hibernate.Hibernate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class IntervalsService {

    private final IntervalsRepository credentialsRepository;
    private final UsuarioRepository usuarioRepository; // <-- Añadido de nuevo
    private final RestClient restClient;
    private final UserSession userSession;
    private final EntrenamientoRepository entrenamientoRepository;
    private final EntrenamientoMapper mapper;

    public IntervalsService(IntervalsRepository credentialsRepository, UsuarioRepository usuarioRepository, UserSession userSession, EntrenamientoRepository entrenamientoRepository, EntrenamientoMapper mapper) {
        this.credentialsRepository = credentialsRepository;
        this.usuarioRepository = usuarioRepository;
        this.userSession = userSession;
        this.entrenamientoRepository = entrenamientoRepository;
        this.mapper = mapper;
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



    @Transactional
    public void sincronizacionBackground(Usuario usuarioSession) {

        try {
            System.out.println("Iniciando sincronización con Intervals...");

            obtenerCredenciales(usuarioSession).ifPresent(creds -> {
                // 1. Validar y traer datos del atleta (Peso, etc.)
                IntervalsAthleteDTO atleta = probarConexion(usuarioSession);
                sincronizarDatosAtleta(usuarioSession, atleta);

                // 2. EL ESLABÓN PERDIDO: Descargar el historial (Array general de actividades)
                System.out.println("Buscando nuevas carreras...");
                List<IntervalsActivityDTO> historialDescargado = descargaHistorialActividades(usuarioSession);

                // 3. Guardar en Base de Datos Local
                if (historialDescargado != null && !historialDescargado.isEmpty()) {
                    guardarHistorialEnBD(usuarioSession, historialDescargado);
                }

                System.out.println("Sincronización completada con éxito. Base de datos actualizada.");
            });
        } catch (Exception e) {
            System.out.println("Error al sincronizar con Intervals: " + e.getMessage());
            throw new RuntimeException("Fallo en la sincronización: " + e.getMessage());
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


    public List<IntervalsActivityDTO> descargaHistorialActividades(Usuario usuarioSession) {

        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        IntervalsCredentials creds = credentialsRepository.findByUsuario(usuarioGestionado)
                .orElseThrow(() -> new RuntimeException("El usuario no tiene credenciales"));

        String authRaw = "API_KEY:" + creds.getIntervalsApiKey();
        String encondeAuth = Base64.getEncoder().encodeToString(authRaw.getBytes());

        try {
            System.out.println("[PULL] Descargando historial de actividades de Intervals...");

            IntervalsActivityDTO[] actividadesArray = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/athlete/{id}/activities")
                            .queryParam("oldest", "2000-01-01T00:00:00")
                            .build(creds.getIntervalsId()))
                    .header("Authorization", "Basic " + encondeAuth)
                    .retrieve()
                    .body(IntervalsActivityDTO[].class);

            if (actividadesArray == null || actividadesArray.length == 0) {
                return List.of();
            }
            System.out.println("[PULL] Éxito. Descargadas " + actividadesArray.length + " actividades");
            return Arrays.asList(actividadesArray);
        } catch (Exception e) {
            System.err.println("[PULL ERROR] Fallo al descargar actividades: " + e.getMessage());
            throw new RuntimeException("No se pudieron descargar las actividades.");
        }

    }

    public void guardarHistorialEnBD(Usuario usuarioSession, List<IntervalsActivityDTO> historial) {

        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int duplicados = 0;
        int nuevos = 0;
        for (IntervalsActivityDTO dto : historial) {
            if (entrenamientoRepository.existsByIntervalsId(dto.id())) {
                duplicados++;
                continue;
            }
            Entrenamiento entidad = mapper.toEntity(dto);

            entidad.setUsuario(usuarioGestionado);

            entrenamientoRepository.save(entidad);
            nuevos++;
        }
        System.out.println("[DB] Sincronización finalizada. Nuevos: " + nuevos + " | Saltados: " + duplicados);
    }

    public IntervalsActivityDTO descargarDetalleActividad(Usuario usuarioSession, String activityId) {
        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        IntervalsCredentials creds = credentialsRepository.findByUsuario(usuarioGestionado)
                .orElseThrow(() -> new RuntimeException("Sin credenciales"));

        String authRaw = "API_KEY:" + creds.getIntervalsApiKey();
        String encondeAuth = Base64.getEncoder().encodeToString(authRaw.getBytes());

        // EL SECRETO REVELADO: El parámetro ?intervals=true es OBLIGATORIO para que el servidor no oculte el array
        return restClient.get()
                .uri("/activity/{id}?intervals=true", activityId)
                .header("Authorization", "Basic " + encondeAuth)
                .retrieve()
                .body(IntervalsActivityDTO.class);
    }

    @Transactional
    public Entrenamiento obtenerEntrenamientoConDetalles(Usuario usuarioSession, Long idEntrenamiento) {
        Entrenamiento entreno = entrenamientoRepository.findById(idEntrenamiento)
                .orElseThrow(() -> new RuntimeException("Entrenamiento no encontrado"));

        if (entreno instanceof EntrenamientoCarrera carrera) {

            // Protección vital: Si Hibernate devuelve la lista en null, la inicializamos
            if (carrera.getVueltas() == null) {
                carrera.setVueltas(new java.util.ArrayList<>());
            } else {
                Hibernate.initialize(carrera.getVueltas());
            }

            // LA MAGIA ARQUITECTÓNICA
            if (carrera.getVueltas().isEmpty() && carrera.getIntervalsId() != null) {
                System.out.println("[API] Vueltas vacías en BD. Solicitando detalle a Intervals...");

                IntervalsActivityDTO detalleCompleto = descargarDetalleActividad(usuarioSession, carrera.getIntervalsId());

                if (detalleCompleto != null && detalleCompleto.vueltas() != null) {
                    List<VueltaCarrera> nuevasVueltas = mapper.mapearVueltas(detalleCompleto.vueltas(), carrera);

                    if (!nuevasVueltas.isEmpty()) {
                        carrera.getVueltas().addAll(nuevasVueltas);
                        entrenamientoRepository.save(carrera);
                        System.out.println("[DB] ¡ÉXITO! " + nuevasVueltas.size() + " vueltas guardadas.");
                    } else {
                        System.out.println("[DB] El mapper devolvió 0 vueltas. Revisa la estructura del DTO.");
                    }
                } else {
                    System.out.println("[DB ERROR] La API no devolvió la lista de vueltas en el DTO (es null).");
                }
            } else {
                System.out.println("[CACHE] Las vueltas ya estaban en base de datos. Cargando...");
            }
        }
        return entreno;
    }

}