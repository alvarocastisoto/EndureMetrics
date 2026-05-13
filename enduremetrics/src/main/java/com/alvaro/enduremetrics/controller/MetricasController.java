package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.service.MetricasService;
import com.alvaro.enduremetrics.session.UserSession;
import com.alvaro.enduremetrics.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class MetricasController {

    // --- FXML INJECTIONS ---
    @FXML private Label vo2maxLabel;
    @FXML private Label pred5kLabel;
    @FXML private Label pred10kLabel;
    @FXML private Label pred21kLabel;
    @FXML private Label pred42kLabel;

    @FXML private Label trimpLabel;
    @FXML private Label trimpEstadoLabel;

    @FXML private Label tsbLabel;
    @FXML private Label tsbEstadoLabel;

    @FXML private VBox zonasContainer;
    @FXML private LineChart<String, Number> vo2Chart;

    private final MetricasService metricasService;
    private final UserSession userSession;

    public MetricasController(MetricasService metricasService, UserSession userSession) {
        this.metricasService = metricasService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        Usuario usuario = userSession.getUsuarioLogueado();

        // 1. VO2Max y Predicciones
        Double vo2Max = metricasService.calcularVo2MaxReciente(usuario);
        pintarVo2Max(vo2Max);
        pintarPredicciones(vo2Max);

        // 2. Carga y Estado de Forma
        pintarCargaSemanal(usuario);
        pintarEstadoDeForma(usuario);

        // 3. Fisiología y Evolución
        pintarZonas(usuario);
        pintarGrafica(usuario);
    }

    // --- MÉTODOS DE RENDERIZADO VISUAL ---

    private void pintarVo2Max(Double vo2Max) {
        if (vo2Max != null) {
            vo2maxLabel.setText(String.format("%.1f", vo2Max));
        } else {
            vo2maxLabel.setText("--.-");
        }
    }

    private void pintarPredicciones(Double vo2Max) {
        pred5kLabel.setText(metricasService.estimarRitmo(vo2Max, 5000));
        pred10kLabel.setText(metricasService.estimarRitmo(vo2Max, 10000));
        pred21kLabel.setText(metricasService.predecirLargaDistancia(vo2Max, 21097));
        pred42kLabel.setText(metricasService.predecirLargaDistancia(vo2Max, 42195));
    }

    private void pintarCargaSemanal(Usuario usuario) {
        Double trimp = metricasService.calcularCargaSemanalTRIMP(usuario);

        if (trimp == null || trimp == 0.0) {
            trimpLabel.setText("---");
            trimpEstadoLabel.setText("Sin datos recientes");
            trimpEstadoLabel.setStyle("-fx-text-fill: #94a3b8;");
            return;
        }

        trimpLabel.setText(String.format("%.0f", trimp));

        if (trimp < 300) {
            trimpEstadoLabel.setText("Recuperación");
            trimpEstadoLabel.setStyle("-fx-text-fill: #3b82f6;");
        } else if (trimp <= 700) {
            trimpEstadoLabel.setText("Productiva");
            trimpEstadoLabel.setStyle("-fx-text-fill: #10b981;");
        } else {
            trimpEstadoLabel.setText("Riesgo de Lesión");
            trimpEstadoLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private void pintarEstadoDeForma(Usuario usuario) {
        Double tsb = metricasService.calcularTsb(usuario);

        if (tsb == null || (tsb == 0.0 && metricasService.calcularCargaSemanalTRIMP(usuario) == 0.0)) {
            tsbLabel.setText("---");
            tsbEstadoLabel.setText("Faltan datos");
            tsbEstadoLabel.setStyle("-fx-text-fill: #94a3b8;");
            return;
        }

        long tsbRedondeado = Math.round(tsb);
        String formatoSímbolo = tsbRedondeado > 0 ? "+" + tsbRedondeado : String.valueOf(tsbRedondeado);

        tsbLabel.setText(formatoSímbolo);

        // Lógica de estados TSB
        if (tsbRedondeado > 5) {
            tsbEstadoLabel.setText("Frescura (Peaking)");
            tsbLabel.setStyle("-fx-text-fill: #3b82f6;"); // Azul
            tsbEstadoLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        } else if (tsbRedondeado >= -10) {
            tsbEstadoLabel.setText("Mantenimiento");
            tsbLabel.setStyle("-fx-text-fill: #64748b;"); // Gris
            tsbEstadoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        } else if (tsbRedondeado >= -30) {
            tsbEstadoLabel.setText("Productivo");
            tsbLabel.setStyle("-fx-text-fill: #f59e0b;"); // Naranja
            tsbEstadoLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        } else {
            tsbEstadoLabel.setText("Riesgo / Sobrecarga");
            tsbLabel.setStyle("-fx-text-fill: #ef4444;"); // Rojo
            tsbEstadoLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
    }

    private void pintarZonas(Usuario usuario) {
        Map<String, String> zonas = metricasService.calcularZonasKarvonen(usuario);

        if (zonas == null) {
            zonasContainer.getChildren().add(new Label("Configura tus FC en el perfil"));
            return;
        }

        zonasContainer.getChildren().clear();
        zonas.forEach((zona, rango) -> {
            HBox fila = new HBox(10);
            Label lblZona = new Label(zona + ":");
            lblZona.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
            Label lblRango = new Label(rango);
            lblRango.setStyle("-fx-text-fill: #64748b;");

            fila.getChildren().addAll(lblZona, lblRango);
            zonasContainer.getChildren().add(fila);
        });
    }

    private void pintarGrafica(Usuario usuario) {
        Map<LocalDate, Double> datos = metricasService.obtenerHistoricoVo2Max(usuario);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (datos != null && !datos.isEmpty()) {
            datos.forEach((fecha, valor) -> {
                series.getData().add(new XYChart.Data<>(fecha.toString(), valor));
            });
        }

        vo2Chart.getData().clear();
        vo2Chart.getData().add(series);
    }
}