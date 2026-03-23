package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCiclismo;
import com.alvaro.enduremetrics.service.EntrenamientoService;
import com.alvaro.enduremetrics.session.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class EntrenamientosController {

    private final EntrenamientoService entrenamientoService;
    private final UserSession userSession;

    @FXML
    private TableView<Entrenamiento> tablaEntrenamientos;
    @FXML
    private TableColumn<Entrenamiento, String> colFecha;
    @FXML
    private TableColumn<Entrenamiento, String> colDeporte;
    @FXML
    private TableColumn<Entrenamiento, String> colDistancia;
    @FXML
    private TableColumn<Entrenamiento, String> colTiempo;

    // Formateador estándar europeo para tu masterclass
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EntrenamientosController(EntrenamientoService entrenamientoService, UserSession userSession) {
        this.entrenamientoService = entrenamientoService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        // 1. FECHA: Parseamos el LocalDateTime a String legible
        colFecha.setCellValueFactory(cell -> {
            if (cell.getValue().getFechaInicio() != null) {
                return new SimpleStringProperty(cell.getValue().getFechaInicio().format(formatter));
            }
            return new SimpleStringProperty("Sin fecha");
        });

        // 2. DEPORTE: Aprovechamos el Polimorfismo (Java 17+ Pattern Matching)
// 2. DEPORTE: Usamos if-else con instanceof (Estándar en Java 17)
        colDeporte.setCellValueFactory(cell -> {
            Entrenamiento ent = cell.getValue();
            String deporte;

            if (ent instanceof EntrenamientoCiclismo) {
                deporte = "Ciclismo";
            } else if (ent instanceof EntrenamientoCarrera) {
                deporte = "Carrera";
            } else {
                deporte = "General/Fuerza";
            }

            return new SimpleStringProperty(deporte);
        });

        // 3. DISTANCIA: Blindaje contra nulos y conversión de Metros a Kilómetros
        colDistancia.setCellValueFactory(cell -> {
            Double distanciaMetros = cell.getValue().getDistancia();
            if (distanciaMetros == null || distanciaMetros == 0.0) {
                return new SimpleStringProperty("---"); // Actividades sin distancia (ej. Pesas)
            }
            double kilometros = distanciaMetros / 1000.0;
            return new SimpleStringProperty(String.format("%.2f km", kilometros));
        });

        // 4. TIEMPO: Convertimos segundos a formato HH:mm:ss
        colTiempo.setCellValueFactory(cell -> {
            Integer segundos = cell.getValue().getTiempoMovimiento();
            if (segundos == null || segundos == 0) {
                return new SimpleStringProperty("---");
            }
            Duration duracion = Duration.ofSeconds(segundos);
            String formateado = String.format("%02d:%02d:%02d",
                    duracion.toHours(),
                    duracion.toMinutesPart(),
                    duracion.toSecondsPart());
            return new SimpleStringProperty(formateado);
        });
    }

    private void cargarDatos() {
        if (!userSession.haySesionActiva()) return;

        List<Entrenamiento> historial = entrenamientoService.obtenerHistorial(userSession.getUsuarioLogueado());

        // Volcamos la lista de Java normal a la lista reactiva de JavaFX
        ObservableList<Entrenamiento> datosReactivos = FXCollections.observableArrayList(historial);
        tablaEntrenamientos.setItems(datosReactivos);
    }
}