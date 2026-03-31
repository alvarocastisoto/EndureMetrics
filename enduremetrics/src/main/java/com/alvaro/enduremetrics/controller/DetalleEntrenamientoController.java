package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCiclismo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;

@Controller
public class DetalleEntrenamientoController {

    @FXML
    private Label lblDeporte;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblDistancia;
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

        // 1. Carga de Entrenamiento (TSS)
        if (entreno.getCargaTss() != null) { // Ajusta el getCargaTss() al nombre real de tu entidad
            lblTss.setText(String.valueOf(entreno.getCargaTss()));
        } else {
            lblTss.setText("N/A");
        }

        // 2. Frecuencia Cardíaca Media
        if (entreno.getFrecuenciaCardiacaMedia() != null) {
            lblFrecCardiaca.setText(entreno.getFrecuenciaCardiacaMedia() + " ppm");
        } else {
            lblFrecCardiaca.setText("N/A");
        }

        // 3. Desnivel Acumulado
        if (entreno.getDesnivelPositivo() != null) {
            lblDesnivel.setText(entreno.getDesnivelPositivo() + " m");
        } else {
            lblDesnivel.setText("0 m");
        }

        // 4. Potencia (Aquí usamos el polimorfismo que mencionamos antes)
        if (entreno instanceof EntrenamientoCiclismo) {
            EntrenamientoCiclismo bici = (EntrenamientoCiclismo) entreno;
            if (bici.getPotenciaNormalizada() != null) {
                // Destacamos la normalizada que es la que importa
                lblPotencia.setText(bici.getPotenciaNormalizada() + " W (NP)");
            } else if (bici.getPotenciaMedia() != null) {
                lblPotencia.setText(bici.getPotenciaMedia() + " W (Med)");
            } else {
                lblPotencia.setText("N/A");
            }
        } else {
            // Si es correr o gimnasio y no hay potenciómetro
            lblPotencia.setText("-");
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