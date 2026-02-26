package com.alvaro.enduremetrics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

import com.alvaro.enduremetrics.session.UserSession;
import com.alvaro.enduremetrics.entity.Usuario;

import java.time.LocalDate;

@Controller
public class ProfileController {

    private final UserSession userSession;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField pesoField;
    @FXML
    private TextField alturaField;
    @FXML
    private TextField fcmField;
    @FXML
    private DatePicker fechaNacimientoPicker;
    @FXML
    private Label mensajeLabel;

    public ProfileController(UserSession userSession) {
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        if (userSession.haySesionActiva()) {
            Usuario usuario = userSession.getUsuarioLogueado();
            usernameField.setText(usuario.getUsername());

            // if (usuario.getPeso().toString() != null)
            // pesoField.setText(usuario.getPeso().toString());
            if (usuario.getAltura() != null) {
                alturaField.setText(usuario.getAltura().toString());
            }
            if (usuario.getFechaNacimiento() != null)
                fechaNacimientoPicker.setValue(usuario.getFechaNacimiento());
        }
    }

    @FXML
    public void guardarPerfil() {
        mensajeLabel.setVisible(false);

        try {
            String pesoStr = pesoField.getText();
            String alturaStr = alturaField.getText();
            String fcmStr = fcmField.getText();
            LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();

            // TODO: Parsear e invocar al Service para guardar en BD

            mostrarMensaje("Perfil actualizado correctamente", "#27ae60");

        } catch (Exception e) {
            mostrarMensaje("Error al guardar el perfil.", "#e74c3c");
        }
    }

    @FXML
    public void abrirModalNuevaZapatilla() {
        System.out.println("Abriendo modal de zapatillas...");
        // TODO: Implementar la lógica del modal aquí
    }

    private void mostrarMensaje(String texto, String colorHex) {
        mensajeLabel.setText(texto);
        mensajeLabel.setTextFill(javafx.scene.paint.Color.web(colorHex));
        mensajeLabel.setVisible(true);
    }
}