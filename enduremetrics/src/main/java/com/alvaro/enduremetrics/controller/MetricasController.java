package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.service.MetricasService;
import com.alvaro.enduremetrics.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;

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
        // 1. Haces el cálculo pesado UNA SOLA VEZ
        Double vo2Max = metricasService.calcularVo2MaxReciente(userSession.getUsuarioLogueado());

        // 2. Repartes el dato ya calculado a métodos pequeños
        pintarVo2Max(vo2Max);
        pintarPredicciones(vo2Max);
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



}




