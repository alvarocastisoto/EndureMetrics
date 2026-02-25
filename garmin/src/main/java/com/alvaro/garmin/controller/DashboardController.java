package com.alvaro.garmin.controller;

import com.alvaro.garmin.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardController {

    private final UserSession userSession;

    @FXML
    private Label bienvenidaLabel;

    public DashboardController(UserSession userSession) {
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        if (userSession.haySesionActiva()) {
            String username = userSession.getUsuarioLogueado().getUsername();
            username = username.substring(0, 1).toUpperCase() + username.substring(1);
            bienvenidaLabel.setText("¡Bienvenido al Dashboard, " + username + "!");
        }
    }
}