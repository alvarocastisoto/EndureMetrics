package com.alvaro.enduremetrics.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.DatePicker;
import org.springframework.stereotype.Controller;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.service.RegisterService;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.context.ApplicationContext;

@Controller
public class RegisterController {

    private final RegisterService registerService;
    private final ApplicationContext springContext;

    public RegisterController(RegisterService registerService, ApplicationContext springContext) {
        this.registerService = registerService;
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        // 1. Ocultamos la tarjeta y la bajamos 30 píxeles
        tarjetaFormulario.setOpacity(0);
        tarjetaFormulario.setTranslateY(30);

        // 2. Animación de Desvanecimiento (Aparecer)
        FadeTransition fade = new FadeTransition(Duration.millis(600), tarjetaFormulario);
        fade.setToValue(1);

        // 3. Animación de Movimiento (Subir a su sitio)
        TranslateTransition translate = new TranslateTransition(Duration.millis(600), tarjetaFormulario);
        translate.setToY(0);

        // Arrancamos las dos a la vez
        fade.play();
        translate.play();
    }

    // ==========================================
    // 1. CAPTURA DE PANTALLA
    // ==========================================
    @FXML
    private Label errorLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField confirmEmailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField pesoField;
    @FXML
    private TextField alturaField;
    @FXML
    private DatePicker fechaNacimientoPicker;
    @FXML
    private ComboBox<String> sexoComboBox;
    @FXML
    private CheckBox termsCheckBox;
    @FXML
    private VBox tarjetaFormulario;

    // ==========================================
    // 2. ACCIÓN DEL BOTÓN PRINCIPAL
    // ==========================================
    @FXML
    public void onRegisterClick() {
        limpiarErrores();

        // 1. Validaciones de texto
        if (estaVacio(usernameField, "El nombre de usuario es obligatorio."))
            return;
        if (estaVacio(emailField, "El correo electrónico es obligatorio."))
            return;
        if (emailsNoCoinciden())
            return;
        if (estaVacio(passwordField, "La contraseña es obligatoria."))
            return;
        if (contraseñasNoCoinciden())
            return;
        if (estaVacio(pesoField, "El peso es obligatorio."))
            return;
        if (estaVacio(alturaField, "La altura es obligatoria."))
            return;

        // 2. Validación de Fecha de Nacimiento (Limpio y seguro)
        LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();
        if (fechaNacimiento == null) {
            mostrarError(fechaNacimientoPicker, "La fecha de nacimiento es obligatoria.");
            return;
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            mostrarError(fechaNacimientoPicker, "La fecha de nacimiento no puede ser futura.");
            return;
        }

        // 3. Validaciones de ComboBox y CheckBox
        if (sexoComboBox.getValue() == null) {
            mostrarError(sexoComboBox, "Debes seleccionar un sexo.");
            return;
        }
        if (!termsCheckBox.isSelected()) {
            mostrarError(null, "Debes aceptar los términos y condiciones.");
            return;
        }

        // 4. Parseo de números
        Double peso;
        Integer altura;

        try {
            peso = Double.parseDouble(pesoField.getText());
        } catch (NumberFormatException e) {
            mostrarError(pesoField, "El peso debe ser un número válido (ej: 75.5).");
            return;
        }

        try {
            altura = Integer.parseInt(alturaField.getText());
        } catch (NumberFormatException e) {
            mostrarError(alturaField, "La altura debe ser un número entero (ej: 180).");
            return;
        }

        // 5. Crear el objeto Usuario con los datos correctos
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(usernameField.getText());
        nuevoUsuario.setEmail(emailField.getText());
        nuevoUsuario.setPassword(passwordField.getText());
        nuevoUsuario.setPeso(peso);
        nuevoUsuario.setAltura(altura);
        nuevoUsuario.setFechaNacimiento(fechaNacimiento);
        nuevoUsuario.setSexo(sexoComboBox.getValue());

        // 6. Enviar a base de datos
        try {
            registerService.registrarNuevoUsuario(nuevoUsuario);
            volverLogin();
        } catch (IllegalArgumentException e) {
            errorLabel.setTextFill(javafx.scene.paint.Color.web("#e74c3c"));
            mostrarError(null, e.getMessage());
        } catch (Exception e) {
            errorLabel.setTextFill(javafx.scene.paint.Color.web("#e74c3c"));
            mostrarError(null, "Error del servidor al registrar. Inténtalo más tarde.");
        }
    }

    // ==========================================
    // 3. HELPERS DE VALIDACIÓN
    // ==========================================
    private boolean estaVacio(TextField campo, String mensajeError) {
        if (campo.getText().isBlank()) {
            mostrarError(campo, mensajeError);
            return true;
        }
        return false;
    }

    private boolean emailsNoCoinciden() {
        if (!emailField.getText().equals(confirmEmailField.getText())) {
            mostrarError(confirmEmailField, "Los correos electrónicos no coinciden.");
            return true;
        }
        return false;
    }

    private boolean contraseñasNoCoinciden() {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            mostrarError(confirmPasswordField, "Las contraseñas no coinciden.");
            return true;
        }
        return false;
    }

    @FXML
    public void volverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) errorLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 4. MÉTODOS VISUALES (Con CSS Classes)
    // ==========================================
    private void limpiarErrores() {
        errorLabel.setText("");
        errorLabel.setVisible(false);

        usernameField.getStyleClass().remove("error-field");
        emailField.getStyleClass().remove("error-field");
        confirmEmailField.getStyleClass().remove("error-field");
        passwordField.getStyleClass().remove("error-field");
        confirmPasswordField.getStyleClass().remove("error-field");
        pesoField.getStyleClass().remove("error-field");
        alturaField.getStyleClass().remove("error-field");
        fechaNacimientoPicker.getStyleClass().remove("error-field");
        sexoComboBox.getStyleClass().remove("error-field");
    }

    private void mostrarError(javafx.scene.control.Control campoFallo, String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);

        if (campoFallo != null) {
            if (!campoFallo.getStyleClass().contains("error-field")) {
                campoFallo.getStyleClass().add("error-field");
            }
            campoFallo.requestFocus();
        }
    }
}