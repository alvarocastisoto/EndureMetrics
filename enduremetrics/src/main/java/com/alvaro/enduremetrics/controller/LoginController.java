package com.alvaro.enduremetrics.controller;

import java.io.IOException;

import com.alvaro.enduremetrics.service.IntervalsService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.service.LoginService;
import com.alvaro.enduremetrics.session.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField; // Corregido: Es un PasswordField
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class LoginController {

    private final ApplicationContext springContext;
    private final LoginService loginService;
    private final UserSession userSession;
    private final IntervalsService intervalsService;
    public LoginController(ApplicationContext springContext, LoginService loginService, UserSession userSession, IntervalsService intervalsService) {
        this.springContext = springContext;
        this.loginService = loginService;
        this.userSession = userSession;
        this.intervalsService = intervalsService;
    }

    @FXML
    private Label errorLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField; // Corregido

    @FXML
    public void onLoginClick() {
        login();
    }

    @FXML
    public void irAlRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/registro-view.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        // 0. Limpiar intentos anteriores
        limpiarErrores();

        // 1. Validar que no haya campos vacíos
        if (usernameField.getText().isBlank()) {
            mostrarError(usernameField, "Por favor, introduce tu usuario.");
            return;
        }
        if (passwordField.getText().isBlank()) {
            mostrarError(passwordField, "Por favor, introduce tu contraseña.");
            return;
        }

        try {
            // Validar en BBDD local (Esto es súper rápido, va en el hilo principal)
            Usuario usuarioLogueado = loginService.validarCredenciales(usernameField.getText(), passwordField.getText());
            userSession.setUsuarioLogueado(usuarioLogueado);

            // 2. Feedback visual inmediato y bloqueo de inputs para evitar doble clic
            errorLabel.setTextFill(javafx.scene.paint.Color.web("#27ae60")); // Verde éxito
            mostrarError(null, "¡Logueado! Sincronizando con Intervals...");
            usernameField.setDisable(true);
            passwordField.setDisable(true);

            // 3. Crear el hilo secundario para la API
            javafx.concurrent.Task<Void> tareaSincronizacion = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    // ESTO ES LO QUE TARDA: Peticiones HTTP y guardado en BBDD
                    intervalsService.sincronizacionBackground(usuarioLogueado);
                    return null;
                }
            };

            // 4. Qué hacer cuando la API termine con éxito (Volver al Hilo UI)
            tareaSincronizacion.setOnSucceeded(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
                    loader.setControllerFactory(springContext::getBean);
                    Parent root = loader.load();
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setMaximized(true);
                    stage.centerOnScreen();
                } catch (IOException e) {
                    mostrarError(null, "Error al cargar la pantalla principal.");
                    e.printStackTrace();
                }
            });

            // 5. Qué hacer si la API de Intervals falla (ej. sin internet)
            tareaSincronizacion.setOnFailed(event -> {
                errorLabel.setTextFill(javafx.scene.paint.Color.web("#e67e22")); // Naranja aviso
                mostrarError(null, "Fallo al sincronizar. Entrando en modo local...");

                // Opcional: Podrías redirigirlo al main-view igualmente copiando el bloque try-catch de arriba
                // para que use la app offline.
                tareaSincronizacion.getException().printStackTrace();

                // Desbloqueamos los campos por si quiere reintentar
                usernameField.setDisable(false);
                passwordField.setDisable(false);
            });

            // 6. ¡Arrancar el hilo!
            new Thread(tareaSincronizacion).start();

        } catch (IllegalArgumentException e) {
            // Aquí atrapamos el error "Usuario o contraseña incorrectos" del Service
            mostrarError(null, e.getMessage());
        } catch (Exception e) {
            // Cualquier otro fallo grave
            mostrarError(null, "Error interno del servidor.");
            e.printStackTrace();
        }
    }

    // ==========================================
    // HELPERS VISUALES
    // ==========================================
    private void limpiarErrores() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setTextFill(javafx.scene.paint.Color.web("#e74c3c")); // Restaurar rojo

        String estiloNormal = "-fx-background-radius: 5; -fx-padding: 8;";
        usernameField.setStyle(estiloNormal);
        passwordField.setStyle(estiloNormal);
    }

    private void mostrarError(javafx.scene.control.Control campoFallo, String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);

        if (campoFallo != null) {
            String estiloActual = campoFallo.getStyle();
            campoFallo
                    .setStyle(estiloActual + " -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
            campoFallo.requestFocus();
        }
    }
}