package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.service.MetricasService;
import com.alvaro.enduremetrics.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class MetricasController {

    @FXML
    private Label vo2maxLabel;
    @FXML
    private Label pred5kLabel;
    @FXML
    private Label pred10kLabel;

    private final MetricasService metricasService;
    private final UserSession userSession;

    public MetricasController(MetricasService metricasService, UserSession userSession) {
        this.metricasService = metricasService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        Double vo2Max = metricasService.calcularVo2MaxReciente(userSession.getUsuarioLogueado());

        pintarVo2Max(vo2Max);
        pintarPredicciones(vo2Max);
        pintarZonas(userSession.getUsuarioLogueado());
    }

    private void pintarVo2Max(Double vo2Max) {
        if (vo2Max != null) {
            vo2maxLabel.setText(String.format("%.1f", vo2Max));
        } else {
            vo2maxLabel.setText("--.-");
        }
    }

    private void pintarPredicciones(Double vo2Max) {
        // El Service ya no va a la BD, solo hace matemáticas rápidas
        pred5kLabel.setText(metricasService.estimarRitmo(vo2Max, 5000));
        pred10kLabel.setText(metricasService.estimarRitmo(vo2Max, 10000));
    }

    @FXML private VBox zonasContainer;
    @FXML private LineChart<String, Number> vo2Chart;

    private void pintarZonas(Usuario usuario) {
        Map<String, String> zonas = metricasService.calcularZonasKarvonen(usuario);
        if (zonas == null) return;

        zonasContainer.getChildren().clear();
        zonas.forEach((zona, rango) -> {
            HBox fila = new HBox(10);
            Label lblZona = new Label(zona + ":");
            lblZona.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");
            Label lblRango = new Label(rango);
            fila.getChildren().addAll(lblZona, lblRango);
            zonasContainer.getChildren().add(fila);
        });
    }

    private void pintarGrafica(Usuario usuario) {
        Map<LocalDate, Double> datos = metricasService.obtenerHistoricoVo2Max(usuario);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        datos.forEach((fecha, valor) -> {
            series.getData().add(new XYChart.Data<>(fecha.toString(), valor));
        });

        vo2Chart.getData().clear();
        vo2Chart.getData().add(series);
    }

}




