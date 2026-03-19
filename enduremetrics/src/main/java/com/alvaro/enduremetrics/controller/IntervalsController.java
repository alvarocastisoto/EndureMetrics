package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.dto.intervals.IntervalsAthleteDTO;
import com.alvaro.enduremetrics.service.IntervalsService;
import com.alvaro.enduremetrics.session.UserSession;
import com.alvaro.enduremetrics.util.ViewUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

@Controller
public class IntervalsController {

    private final UserSession userSession;
    private final IntervalsService intervalsService;
    // ¡FUERA EL REPOSITORIO DE AQUÍ!

    @FXML
    private TextField athleteIdField;
    @FXML
    private PasswordField apiKeyField;
    @FXML
    private Label mensajeLabel;

    public IntervalsController(UserSession userSession, IntervalsService intervalsService) {
        this.userSession = userSession;
        this.intervalsService = intervalsService;
    }

    @FXML
    public void initialize() {
        cargarAPI();
    }

    @FXML
    public void conectarIntervals() {
        if (!userSession.haySesionActiva()) return;

        String athleteId = athleteIdField.getText();
        String apiKey = apiKeyField.getText();

        if (athleteId == null || athleteId.isBlank() || apiKey == null || apiKey.isBlank()) {
            ViewUtils.mostrarMensaje(mensajeLabel, "El ID y la API Key son obligatorios.", "#e74c3c");
            return;
        }

        try {
            ViewUtils.mostrarMensaje(mensajeLabel, "Conectando con Intervals...", "#f59e0b");

            // 1. PRIMERO guardamos las credenciales en PostgreSQL
            intervalsService.agregarInvervals(userSession.getUsuarioLogueado(), athleteId, apiKey);

            // 2. SEGUNDO hacemos el ping a la API (que leerá las credenciales recién guardadas)
            IntervalsAthleteDTO atleta = intervalsService.probarConexion(userSession.getUsuarioLogueado());


            System.out.println("¡Éxito! Atleta conectado: " + atleta.firstname());
            ViewUtils.mostrarMensaje(mensajeLabel, "¡Conectado como " + atleta.firstname() + "!", "#27ae60");
            java.util.List<com.alvaro.enduremetrics.dto.intervals.IntervalsActivityDTO> historial =
                    intervalsService.descargaHistorialActividades(userSession.getUsuarioLogueado());
            if (!historial.isEmpty()) {
                System.out.println("--- PRIMER ENTRENAMIENTO ENCONTRADO ---");
                System.out.println("Deporte: " + historial.get(0).type());
                System.out.println("Fecha: " + historial.get(0).fechaInicio());
                System.out.println("Distancia: " + (historial.get(0).distance() / 1000.0) + " km");
            }
        } catch (IllegalArgumentException e) {
            ViewUtils.mostrarMensaje(mensajeLabel, e.getMessage(), "#e74c3c");
        } catch (Exception e) {
            ViewUtils.mostrarMensaje(mensajeLabel, "Error de red al conectar.", "#e74c3c");
            e.printStackTrace();
        }
    }

    private void cargarAPI() {
        if (!userSession.haySesionActiva()) return;

        // Si el Optional trae datos, los pintamos. Si viene vacío, no hace nada (campos en blanco para usuario nuevo)
        intervalsService.obtenerCredenciales(userSession.getUsuarioLogueado())
                .ifPresent(dto -> {
                    athleteIdField.setText(dto.athleteId());
                    apiKeyField.setText(dto.apiKey());
                });
    }
}