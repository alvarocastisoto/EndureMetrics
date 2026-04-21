package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCiclismo;
import com.alvaro.enduremetrics.service.EntrenamientoService;
import com.alvaro.enduremetrics.service.IntervalsService;
import com.alvaro.enduremetrics.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CalendarioController {
    private final EntrenamientoService entrenamientoService;
    private final UserSession userSession;
    private final IntervalsService intervalsService;
    @FXML
    private GridPane gridCalendario;
    @FXML
    private Label labelMesAnio;

    private YearMonth mesActual = YearMonth.now();
    private final ApplicationContext springContext;

    public CalendarioController(EntrenamientoService entrenamientoService, UserSession userSession, IntervalsService intervalsService,
                                ApplicationContext springContext) {
        this.entrenamientoService = entrenamientoService;
        this.userSession = userSession;
        this.intervalsService = intervalsService;
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        if (mesActual == null)
            mesActual = YearMonth.now();
        dibujarCalendario();
    }

    private void dibujarCalendario() {
        if (gridCalendario == null) {
            System.err.println("ERROR: El calendario está vacío.");
            return;
        }

        gridCalendario.getChildren()
                .removeIf(nodo -> GridPane.getRowIndex(nodo) != null && GridPane.getRowIndex(nodo) > 0);
        labelMesAnio.setText(mesActual.getMonth().name() + " " + mesActual.getYear());

        List<Entrenamiento> entrenos = entrenamientoService.obtenerHistorialDelMes(userSession.getUsuarioLogueado(),
                mesActual);

        Map<LocalDate, List<Entrenamiento>> mapaEntrenos = entrenos.stream()
                .collect(Collectors.groupingBy(e -> e.getFechaInicio().toLocalDate()));

        // Lógica de posicionamiento
        LocalDate primerDiaMes = mesActual.atDay(1);
        int desplazamiento = primerDiaMes.getDayOfWeek().getValue() - 1; // Lunes = 0
        int diasEnMes = mesActual.lengthOfMonth();

        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fechaDia = mesActual.atDay(dia);
            VBox celda = crearCeldaDia(dia, mapaEntrenos.get(fechaDia));

            // Cálculo de coordenadas en el GridPane (7 columnas)
            int columna = (dia + desplazamiento - 1) % 7;
            int fila = (dia + desplazamiento - 1) / 7 + 1;

            gridCalendario.add(celda, columna, fila);
        }
        gridCalendario.getRowConstraints().clear();
        // Creamos una restricción para la fila 0 (cabecera L-D)
        RowConstraints rcHeader = new RowConstraints();
        rcHeader.setPrefHeight(30);
        gridCalendario.getRowConstraints().add(rcHeader);

        // Creamos restricciones para las filas de días (suelen ser 5 o 6)
        int numFilas = (diasEnMes + desplazamiento + 6) / 7;
        for (int i = 0; i < numFilas; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS); // <--- Esto hace que las filas se estiren verticalmente
            rc.setPercentHeight(100.0 / numFilas); // Divide el espacio equitativamente
            gridCalendario.getRowConstraints().add(rc);
        }

    }

    private VBox crearCeldaDia(int numeroDia, List<Entrenamiento> entrenosDia) {
        VBox v = new VBox(3); // Espacio de 3px entre entrenos
        v.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        v.setPrefHeight(100);
        v.setStyle("-fx-border-color: #f1f2f6; -fx-border-width: 0.5; -fx-padding: 5; -fx-background-color: white;");

        // Número del día
        Label lbNum = new Label(String.valueOf(numeroDia));
        lbNum.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11; -fx-font-weight: bold;");
        v.getChildren().add(lbNum);

        if (entrenosDia != null) {
            for (Entrenamiento e : entrenosDia) {
                Label lbEntreno = new Label();
                lbEntreno.setMaxWidth(Double.MAX_VALUE);
                lbEntreno.setStyle(
                        "-fx-font-size: 10; -fx-padding: 2 5 2 5; -fx-background-radius: 3; -fx-text-fill: white;");

                // Lógica de visualización por tipo
                if (e instanceof EntrenamientoCiclismo) {
                    lbEntreno.setText("🚴 " + formatKm(e));
                    lbEntreno.setStyle(lbEntreno.getStyle() + "-fx-background-color: #3498db;"); // Azul
                } else if (e instanceof EntrenamientoCarrera) {
                    lbEntreno.setText("🏃 " + formatKm(e));
                    lbEntreno.setStyle(lbEntreno.getStyle() + "-fx-background-color: #2ecc71;"); // Verde
                } else {
                    lbEntreno.setText("💪 GYM");
                    lbEntreno.setStyle(lbEntreno.getStyle() + "-fx-background-color: #95a5a6;"); // Gris
                }

                lbEntreno.setCursor(javafx.scene.Cursor.HAND);
                lbEntreno.setOnMouseClicked(event -> {
                    event.consume();
                    abrirDetalleEntrenamiento(e, event); // Le pasamos el evento
                });
                v.getChildren().add(lbEntreno);
            }
        }

        // Efecto Hover para que sepa que es interactivo (opcional pero pro)
        v.setOnMouseEntered(event -> v.setStyle(v.getStyle() + "-fx-background-color: #f8f9fa; -fx-cursor: hand;"));
        v.setOnMouseExited(event -> v.setStyle(v.getStyle() + "-fx-background-color: white;"));

        return v;
    }

    @FXML
    public void mesAnterior() {
        mesActual = mesActual.minusMonths(1);
        dibujarCalendario();
    }

    @FXML
    public void mesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        dibujarCalendario();
    }

    private String formatKm(Entrenamiento e) {
        if (e.getDistancia() == null)
            return "";
        return String.format("%.1fk", e.getDistancia() / 1000);
    }

    private void abrirDetalleEntrenamiento(Entrenamiento entrenoIncompleto, javafx.scene.input.MouseEvent event) {
        try {

            Entrenamiento entrenoCompleto = intervalsService.obtenerEntrenamientoConDetalles(userSession.getUsuarioLogueado(), entrenoIncompleto.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/detalle-entrenamiento-view.fxml"));

            // 1. Spring crea el controlador (Inyección de dependencias lista)
            loader.setControllerFactory(springContext::getBean);
            Parent vista = loader.load();

            // 2. EL PASO QUE FALTABA: Recuperamos el controlador y le inyectamos los datos
            // en RAM
            DetalleEntrenamientoController controlador = loader.getController();
            controlador.cargarDatos(entrenoCompleto);

            // 3. Enrutamiento limpio en el panel central
            StackPane panelCentralReal = (StackPane) gridCalendario.getScene().lookup("#panelCentral");
            if (panelCentralReal != null) {
                panelCentralReal.getChildren().setAll(vista);
            } else {
                // Plan B por si el ID en tu main no es exactamente "panelCentral"
                System.err
                        .println("Ojo: No se ha encontrado el #panelCentral. Verifica el fx:id en tu vista principal.");
            }
        } catch (IOException e) {
            System.err.println("Error al inyectar la vista de detalle en el panel central.");
            e.printStackTrace();
        }
    }
}
