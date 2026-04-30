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


        if (vo2Max != null) {
            // 1. Pintamos el VO2Max
            vo2maxLabel.setText(String.format("%.1f", vo2Max));

            // 2. Calculamos y pintamos las predicciones
            String tiempo5k = metricasService.estimarRitmo(userSession.getUsuarioLogueado(), 5000);
            String tiempo10k = metricasService.estimarRitmo(userSession.getUsuarioLogueado(), 10000);

            pred5kLabel.setText(tiempo5k);
            pred10kLabel.setText(tiempo10k);

        } else {
            vo2maxLabel.setText("--.-");
            pred5kLabel.setText("--:--");
            pred10kLabel.setText("--:--");
        }
    }


}


