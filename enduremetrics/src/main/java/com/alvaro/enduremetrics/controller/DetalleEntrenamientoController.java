package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoGimnasio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.Duration;

@Controller
public class DetalleEntrenamientoController {

    @FXML private Label lblDeporte;
    @FXML private Label lblFecha;
    @FXML private Label lblDistancia;
    @FXML private Label lblDuracion;
    @FXML private Label lblTss;
    @FXML private Label lblFrecCardiaca;
    @FXML private Label lblDesnivel;
    @FXML private Label lblPotencia;

    private final ApplicationContext springContext;

    // Inyectamos el contexto de Spring para cuando tengamos que volver al Calendario
    public DetalleEntrenamientoController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void cargarDatos(Entrenamiento entreno) {
        // 1. Datos Universales (Clase Padre)
        lblDeporte.setText(entreno.getClass().getSimpleName().replace("Entrenamiento", ""));

        // Formateo de fecha limpio
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm");
        lblFecha.setText(entreno.getFechaInicio().format(formatter));

        if (entreno.getDistancia() != null) {
            lblDistancia.setText(String.format("%.2f km", entreno.getDistancia() / 1000.0));
        } else {
            lblDistancia.setText("-");
        }

        if (entreno.getTiempoMovimiento() != null) {
            Duration d = Duration.ofSeconds(entreno.getTiempoMovimiento());
            lblDuracion.setText(String.format("%d:%02d:%02d", d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart()));
        } else {
            lblDuracion.setText("-");
        }

        lblTss.setText(entreno.getCargaTss() != null ? String.valueOf(entreno.getCargaTss()) : "N/A");
        lblFrecCardiaca.setText(entreno.getFrecuenciaCardiacaMedia() != null ? entreno.getFrecuenciaCardiacaMedia() + " ppm" : "N/A");

        // 2. Datos Específicos por Biomecánica (Polimorfismo limpio)
        if (entreno instanceof EntrenamientoCarrera carrera) {
            lblDesnivel.setText(carrera.getDesnivelPositivo() != null ? carrera.getDesnivelPositivo() + " m" : "0 m");
            lblPotencia.setText(carrera.getPotenciaCarreraMedia() != null ? carrera.getPotenciaCarreraMedia() + " W" : "N/A");
        } else if (entreno instanceof EntrenamientoGimnasio gym) {
            lblDesnivel.setText("-");
            lblPotencia.setText(gym.getVolumenTotalKg() != null ? gym.getVolumenTotalKg() + " kg (Vol)" : "N/A");
        } else {
            lblDesnivel.setText("-");
            lblPotencia.setText("-");
        }
    }

    @FXML
    public void volverAlCalendario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/entrenamientos-view.fxml"));
            // IMPORTANTÍSIMO: Devolvemos el control a Spring al cargar el calendario
            loader.setControllerFactory(springContext::getBean);
            Parent vistaCalendario = loader.load();

            // Navegación limpia SPA sin destruir el menú lateral
            StackPane panelCentralReal = (StackPane) ((javafx.scene.Node) event.getSource()).getScene().lookup("#panelCentral");
            if (panelCentralReal != null) {
                panelCentralReal.getChildren().setAll(vistaCalendario);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}