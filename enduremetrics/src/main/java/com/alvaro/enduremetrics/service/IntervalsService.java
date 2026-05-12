package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.dto.intervals.*;
import com.alvaro.enduremetrics.entity.IntervalsCredentials;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.VueltaCarrera;
import com.alvaro.enduremetrics.mapper.EntrenamientoMapper;
import com.alvaro.enduremetrics.repository.EntrenamientoCarreraRepository;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class IntervalsService {

    private final IntervalsRepository credentialsRepository;
    private final UsuarioRepository usuarioRepository; // <-- Añadido de nuevo
    private final RestClient restClient;
    private final UserSession userSession;
    private final EntrenamientoRepository entrenamientoRepository;
    private final EntrenamientoMapper mapper;
    private final EntrenamientoCarreraRepository carreraRepository;

    public IntervalsService(IntervalsRepository credentialsRepository, UsuarioRepository usuarioRepository, UserSession userSession, EntrenamientoRepository entrenamientoRepository, EntrenamientoMapper mapper, EntrenamientoCarreraRepository carreraRepository) {
        this.credentialsRepository = credentialsRepository;
        this.usuarioRepository = usuarioRepository;
        this.userSession = userSession;
        this.entrenamientoRepository = entrenamientoRepository;
        this.mapper = mapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://intervals.icu/api/v1")
                .build();
        this.carreraRepository = carreraRepository;
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
        }

        if (atletaDTO.fcReposo() != null) {
            usuarioGestionado.setFcReposo(atletaDTO.fcReposo());
            hayCambios = true; // <-- AÑADIDO
        }
        if (atletaDTO.sportSettings() != null) {
            var settingRun = atletaDTO.sportSettings().stream()
                    .filter(s -> s.types() != null && s.types().contains("Run"))
                    .findFirst();

            // Al hacerlo con un if normal, estamos fuera de la lambda y podemos modificar hayCambios
            if (settingRun.isPresent() && settingRun.get().fcm() != null) {
                usuarioGestionado.setFcMax(settingRun.get().fcm());
                hayCambios = true;
            }
        }

        if (hayCambios) {
            usuarioRepository.save(usuarioGestionado);
            userSession.setUsuarioLogueado(usuarioGestionado); // Refrescamos la sesión aquí también
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
    public void actualizarPerfilIntervals(Usuario usuarioSession, Double nuevoPeso, Double altura, LocalDate fechaNacimiento, String sexoUi, Integer fcMax, Integer fcReposo) {
        try {
            obtenerCredenciales(usuarioSession).ifPresent(creds -> {
                String authRaw = "API_KEY:" + creds.apiKey();
                String encodedAuth = Base64.getEncoder().encodeToString(authRaw.getBytes());

                IntervalsAthleteDTO perfilActual = restClient.get()
                        .uri("/athlete/{id}", creds.athleteId())
                        .header("Authorization", "Basic " + encodedAuth)
                        .retrieve()
                        .body(IntervalsAthleteDTO.class);

                if (perfilActual == null) {
                    System.err.println("[PUSH ERROR] No se pudo descargar el perfil base de Intervals.");
                    return;
                }

                java.util.List<IntervalsSportSettingDTO> settingsActualizados = new java.util.ArrayList<>();

                if (perfilActual.sportSettings() != null) {
                    for (IntervalsSportSettingDTO settingViejo : perfilActual.sportSettings()) {
                        if (settingViejo.types() != null && settingViejo.types().contains("Run")) {
                            // Encontramos el de carrera: Clonamos los datos viejos pero metemos la nueva FC MAX
                            settingsActualizados.add(new IntervalsSportSettingDTO(
                                    settingViejo.id(),             // ¡VITAL! Mantenemos el ID original
                                    settingViejo.types(),
                                    settingViejo.ftp(),
                                    settingViejo.umbralLactato(),
                                    fcMax                          // <-- NUESTRO NUEVO VALOR
                            ));
                        } else {
                            // Es ciclismo, natación, etc. Lo metemos a la lista tal cual
                            settingsActualizados.add(settingViejo);
                        }
                    }
                }

                Double alturaApi = (altura != null) ? altura / 100.0 : null;
                String sexoApi = null;
                if ("Hombre".equals(sexoUi)) sexoApi = "M";
                else if ("Mujer".equals(sexoUi)) sexoApi = "F";

                IntervalsUpdateProfileDTO updateDTO = new IntervalsUpdateProfileDTO(
                        nuevoPeso,
                        alturaApi,
                        fechaNacimiento,
                        sexoApi,
                        fcReposo,             // La FC de reposo va en la raíz
                        settingsActualizados  // La lista que acabamos de reconstruir
                );

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
            e.printStackTrace(); // Para ver exactamente dónde falla si ocurre un error
        }
    }


    public List<IntervalsActivityDTO> descargaHistorialActividades(Usuario usuarioSession) {
        // 1. Obtener el usuario gestionado para evitar problemas con Hibernate (Detached Entity)
        Usuario usuarioGestionado = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscamos la última fecha usando el usuario gestionado
        Optional<LocalDateTime> ultimaFecha = carreraRepository.buscarFechaUltimoEntrenamiento(usuarioGestionado);

        IntervalsCredentials creds = credentialsRepository.findByUsuario(usuarioGestionado)
                .orElseThrow(() -> new RuntimeException("El usuario no tiene credenciales"));

        String authRaw = "API_KEY:" + creds.getIntervalsApiKey();
        String encondeAuth = Base64.getEncoder().encodeToString(authRaw.getBytes());

        // 3. Lógica Delta: ¿Desde cuándo pedimos datos?
        String oldestDate;
        if (ultimaFecha.isPresent()) {
            oldestDate = ultimaFecha.get().toString(); // Se formatea automático a ISO (ej. 2026-05-12T12:00:00)
            System.out.println("[PULL] Sincronización Delta: Pidiendo desde " + oldestDate);
        } else {
            oldestDate = "2000-01-01T00:00:00"; // Fallback para usuario 100% nuevo
            System.out.println("[PULL] Usuario nuevo: Descargando historial completo desde el año 2000.");
        }

        try {
            IntervalsActivityDTO[] actividadesArray = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/athlete/{id}/activities")
                            // 4. Inyectamos la variable dinámica en lugar de la fecha a mano
                            .queryParam("oldest", oldestDate)
                            .build(creds.getIntervalsId()))
                    .header("Authorization", "Basic " + encondeAuth)
                    .retrieve()
                    .body(IntervalsActivityDTO[].class);

            if (actividadesArray == null || actividadesArray.length == 0) {
                System.out.println("[PULL] No hay entrenamientos nuevos en Intervals.");
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