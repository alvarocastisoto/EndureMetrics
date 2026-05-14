package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.MetricaCorporal;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.service.ComposicionService;
import com.alvaro.enduremetrics.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import org.springframework.data.geo.Metric;
import org.springframework.stereotype.Controller;

import java.util.List;

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
    private Usuario usuarioActual;
    public ComposicionController(ComposicionService composicionService, UserSession userSession) {
        this.composicionService = composicionService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        this.usuarioActual = userSession.getUsuarioLogueado();
        // UX: Si es hombre, ocultamos el campo cadera porque la fórmula US Navy no lo usa
        if (usuarioActual.getSexo() != null && usuarioActual.getSexo().equalsIgnoreCase("hombre")) {
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

    @FXML
    private void actualizarGrafica() {


        List<MetricaCorporal> historico = composicionService.obtenerHistoricoReciente(usuarioActual);

        evolucionChart.getData().clear();
        XYChart.Series<String, Number> seriePeso = new XYChart.Series<>();
        seriePeso.setName("Peso (kg)");

        XYChart.Series<String, Number> serieGrasa = new XYChart.Series<>();
        serieGrasa.setName("% Grasa");

        for (MetricaCorporal metrica : historico) {
            String fechaStr = metrica.getFecha().toString();

            seriePeso.getData().add(new XYChart.Data<>(fechaStr, metrica.getPeso()));

            if (metrica.getPorcentajeGrasa() != null) {
                serieGrasa.getData().add(new XYChart.Data<>(fechaStr, metrica.getPorcentajeGrasa()));
            }
        }

        evolucionChart.getData().addAll(seriePeso, serieGrasa);
    }
}