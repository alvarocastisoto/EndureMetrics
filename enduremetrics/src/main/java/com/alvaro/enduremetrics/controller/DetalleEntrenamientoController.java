package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class DetalleEntrenamientoController {

    @FXML private Label lblDeporte;
    @FXML private Label lblFecha;
    @FXML private Label lblDistancia;
    // Añade los @FXML que quieras (tiempo, ID de Intervals, etc.)

    // Este método es llamado desde el CalendarioController ANTES de mostrar la ventana
    public void cargarDatos(Entrenamiento entreno) {
        // Aquí extraemos todo el jugo al objeto que ya teníamos en memoria RAM
        lblDeporte.setText(entreno.getClass().getSimpleName()); // Nos dirá si es Ciclismo o Carrera
        lblFecha.setText(entreno.getFechaInicio().toString());

        if (entreno.getDistancia() != null) {
            lblDistancia.setText(String.format("%.2f km", entreno.getDistancia() / 1000.0));
        } else {
            lblDistancia.setText("Sin distancia");
        }
    }

    @FXML
    public void volverAlCalendario(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/calendario-view.fxml"));
            javafx.scene.Parent vistaCalendario = loader.load();

            javafx.scene.Scene escenaPrincipal = ((javafx.scene.Node) event.getSource()).getScene();
            escenaPrincipal.setRoot(vistaCalendario);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}