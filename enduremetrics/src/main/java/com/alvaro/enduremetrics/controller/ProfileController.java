package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.dto.ProfileDTO;
import com.alvaro.enduremetrics.service.ProfileService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
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
    @FXML
    private ComboBox <String>sexoComboBox;

    public ProfileController(UserSession userSession) {
        this.userSession = userSession;
    }

    @Autowired
    private ProfileService profileService;

    @FXML
    public void initialize() {

        sexoComboBox.getItems().addAll(
                "Hombre",
                "Mujer",
                "Otro",
                "Prefiero no decirlo"
        );
        if (userSession.haySesionActiva()) {
            // Pedimos el DTO al servicio
            ProfileDTO perfil = profileService.obtenerPerfil(userSession.getUsuarioLogueado());

            // Rellenamos la UI
            usernameField.setText(perfil.username()); // Nota: es username(), no getUsername()

            if (perfil.altura() != null) {
                alturaField.setText(perfil.altura().toString());
            }

            if (perfil.fechaNacimiento() != null) {
                fechaNacimientoPicker.setValue(perfil.fechaNacimiento());
            }
            if (perfil.sexo() != null) {
                sexoComboBox.setValue(perfil.sexo());
            }
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