package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.dto.ZapatillaDTO;
import com.alvaro.enduremetrics.service.ZapatillasService;
import com.alvaro.enduremetrics.session.UserSession;
import com.alvaro.enduremetrics.util.ViewUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.util.function.Function;

@Controller
public class ZapatillaModalController {

    @FXML
    private TextField marcaField;
    @FXML
    private TextField modeloField;
    @FXML
    private TextField kmActualesField;
    @FXML
    private TextField kmMaximosField;
    @FXML
    private ComboBox<String> terrenoComboBox;
    @FXML
    private CheckBox activaCheckBox;
    @FXML
    private Label mensajeLabel;
    private final ZapatillasService zapatillasService;
    private final UserSession userSession;

    public ZapatillaModalController(ZapatillasService zapatillasService, UserSession userSession) {
        this.zapatillasService = zapatillasService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        configurarDesplegables();
    }

    @FXML
    public void guardarZapatilla() {
        try {
            // 1. Recogemos los textos puros
            String marca = marcaField.getText();
            String modelo = modeloField.getText();

            // 2. Validación obligatoria
            if (marca == null || marca.isBlank() || modelo == null || modelo.isBlank()) {
                ViewUtils.mostrarMensaje(mensajeLabel, "La marca y el modelo son obligatorios.", "#e74c3c");
                return; // Abortamos el guardado
            }

            // 3. Parseos peligrosos
            Double kmActuales = parsearNumero(kmActualesField.getText(), Double::valueOf);
            Double kmMaximos = parsearNumero(kmMaximosField.getText(), Double::valueOf);

            String terreno = terrenoComboBox.getValue();
            Boolean activa = activaCheckBox.isSelected();

            // 4. Montamos el DTO
            ZapatillaDTO zapatillaDTO = new ZapatillaDTO(marca, modelo, kmActuales, kmMaximos, terreno, activa);

            // 5. Guardamos en la base de datos
            zapatillasService.añadirZapatilla(userSession.getUsuarioLogueado(), zapatillaDTO);

            System.out.println("Zapatilla guardada con éxito en PostgreSQL.");

            // 6. UX: Cerramos el modal automáticamente tras el éxito
            cerrarModal();

        } catch (NumberFormatException e) {
            ViewUtils.mostrarMensaje(mensajeLabel, "Los kilómetros deben ser valores numéricos.", "#e74c3c");
        } catch (Exception e) {
            ViewUtils.mostrarMensaje(mensajeLabel, "Error inesperado al guardar la zapatilla.", "#e74c3c");
            e.printStackTrace(); // Para que tú veas el error real en la consola
        }
    }

    @FXML
    public void cerrarModal() {
        // Obtenemos la ventana actual a través de cualquier componente (ej. el botón o un field)
        Stage stage = (Stage) marcaField.getScene().getWindow();
        stage.close();
    }

    private void configurarDesplegables() {
        ViewUtils.popularComboBox(terrenoComboBox, "Asfalto", "Trail", "Mixto");
    }

    private <T> T parsearNumero(String texto, Function<String, T> conversor) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        // Limpiamos espacios y cambiamos la coma por punto para que Java no pete
        String textoLimpio = texto.trim().replace(",", ".");
        return conversor.apply(textoLimpio);
    }
}