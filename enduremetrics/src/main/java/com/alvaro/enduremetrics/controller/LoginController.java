package com.alvaro.enduremetrics.controller;

import java.io.IOException;

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

    public LoginController(ApplicationContext springContext, LoginService loginService, UserSession userSession) {
        this.springContext = springContext;
        this.loginService = loginService;
        this.userSession = userSession;
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

        // 2. Intentar loguear y atrapar los errores
        try {

            Usuario usuarioLogueado = loginService.validarCredenciales(usernameField.getText(),
                    passwordField.getText());
            userSession.setUsuarioLogueado(usuarioLogueado);
            errorLabel.setTextFill(javafx.scene.paint.Color.web("#27ae60")); // Verde éxito
            mostrarError(null, "¡Conectando...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IllegalArgumentException e) {
            // Aquí atrapamos el error "Usuario o contraseña incorrectos" del Service
            mostrarError(null, e.getMessage());
        } catch (IOException e) {
            // Si el archivo main-view.fxml no existe o falla al cargar
            mostrarError(null, "Error al cargar la pantalla principal.");
            e.printStackTrace();
        } catch (Exception e) {
            // Cualquier otro fallo grave (ej: Base de datos caída)
            mostrarError(null, "Error interno del servidor.");
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