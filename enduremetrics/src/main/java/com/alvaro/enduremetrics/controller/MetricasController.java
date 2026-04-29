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
    MetricasService metricasService;
    UserSession userSession;

    public MetricasController(MetricasService metricasService, UserSession userSession) {
        this.metricasService = metricasService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        pintarMetricas();
    }

    private void pintarMetricas() {
        Double vo2Max = metricasService.calcularVo2MaxReciente(userSession.getUsuarioLogueado());

        if (vo2Max != null) {
            vo2maxLabel.setText(String.format("%.1f", vo2Max));
        } else {
            vo2maxLabel.setText("--.-");
        }
    }
}


