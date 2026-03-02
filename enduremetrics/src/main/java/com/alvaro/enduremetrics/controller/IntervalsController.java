package com.alvaro.enduremetrics.controller;

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

    @FXML private TextField athleteIdField;
    @FXML private PasswordField apiKeyField;
    @FXML private Label mensajeLabel;

    // Inyección por constructor
    public IntervalsController(UserSession userSession) {
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        // En el futuro, si el usuario ya tiene las credenciales guardadas en PostgreSQL,
        // las cargaremos aquí para que no tenga que volver a ponerlas.
    }

    @FXML
    public void conectarIntervals() {
        String athleteId = athleteIdField.getText();
        String apiKey = apiKeyField.getText();

        // 1. Validación de frontend obligatoria
        if (athleteId == null || athleteId.isBlank() || apiKey == null || apiKey.isBlank()) {
            ViewUtils.mostrarMensaje(mensajeLabel, "El ID y la API Key son obligatorios.", "#e74c3c");
            return;
        }

        // 2. Aquí llamaremos al servicio HTTP en el siguiente paso
        System.out.println("Enviando petición a Intervals para el atleta: " + athleteId);
        ViewUtils.mostrarMensaje(mensajeLabel, "Conectando con Intervals...", "#f59e0b"); // Amarillo de carga
    }
}