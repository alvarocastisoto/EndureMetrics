package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.service.ComposicionService;
import com.alvaro.enduremetrics.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import org.springframework.stereotype.Controller;

@Controller
public class ComposicionController {

    @FXML private TextField pesoField;
    @FXML private TextField cuelloField;
    @FXML private TextField cinturaField;
    @FXML private TextField caderaField;
    @FXML private VBox caderaContainer;
    @FXML private Label mensajeLabel;
    @FXML private LineChart<String, Number> evolucionChart;

    private final ComposicionService composicionService;
    private final UserSession userSession;

    public ComposicionController(ComposicionService composicionService, UserSession userSession) {
        this.composicionService = composicionService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        Usuario usuario = userSession.getUsuarioLogueado();

        // UX: Si es hombre, ocultamos el campo cadera porque la fórmula US Navy no lo usa
        if (usuario.getSexo() != null && usuario.getSexo().equalsIgnoreCase("hombre")) {
            caderaContainer.setVisible(false);
            caderaContainer.setManaged(false);
        }

        actualizarGrafica();
    }

    @FXML
    public void guardarMetricas() {
        mensajeLabel.setText(""); // Limpiar errores previos

        try {
            double peso = Double.parseDouble(pesoField.getText().replace(",", "."));
            double cuello = Double.parseDouble(cuelloField.getText().replace(",", "."));
            double cintura = Double.parseDouble(cinturaField.getText().replace(",", "."));

            double cadera = 0.0;
            if (caderaContainer.isVisible() && !caderaField.getText().isBlank()) {
                cadera = Double.parseDouble(caderaField.getText().replace(",", "."));
            }

            // Llamada al Service respetando la arquitectura
            composicionService.registrarMetricas(userSession.getUsuarioLogueado(), peso, cuello, cintura, cadera);

            mensajeLabel.setStyle("-fx-text-fill: #27ae60;"); // Verde éxito
            mensajeLabel.setText("¡Medidas registradas correctamente!");

            // Limpiamos los campos
            pesoField.clear(); cuelloField.clear(); cinturaField.clear(); caderaField.clear();

            // Recargamos la gráfica para ver el nuevo punto
            actualizarGrafica();

        } catch (NumberFormatException e) {
            mensajeLabel.setStyle("-fx-text-fill: #e74c3c;"); // Rojo error
            mensajeLabel.setText("Por favor, introduce solo números válidos.");
        } catch (Exception e) {
            mensajeLabel.setStyle("-fx-text-fill: #e74c3c;");
            mensajeLabel.setText("Error al guardar: " + e.getMessage());
        }
    }

    private void actualizarGrafica() {
        // Aquí llamaremos al Service para que nos dé el histórico de MetricaCorporal
        // y lo mapearemos a dos Series de JavaFX (una para Peso y otra para % Grasa).
        // Lo implementaremos en cuanto me confirmes que la base compila.
    }
}