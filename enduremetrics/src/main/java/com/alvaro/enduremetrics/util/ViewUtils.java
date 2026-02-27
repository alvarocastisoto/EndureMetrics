package com.alvaro.enduremetrics.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;


public class ViewUtils {
    private ViewUtils(){
    }

    @SafeVarargs
    public static <T> void popularComboBox(ComboBox<T> comboBox, T... items){
        if(comboBox != null){
            comboBox.getItems().setAll(items);
        }
    }

    /**
     * Muestra un cuadro de diálogo estándar de JavaFX (Alertas emergentes).
     */
    public static void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido){
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    /**
     * Cambia el texto y el color de un Label en la propia vista.
     * Este es el que está intentando usar tu ProfileController.
     */
    public static void mostrarMensaje(Label label, String mensaje, String colorHex) {
        if (label != null) {
            label.setText(mensaje);
            label.setStyle("-fx-text-fill: " + colorHex + ";");
            label.setVisible(true);
        }
    }


}