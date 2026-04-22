package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.entity.entrenamiento.Entrenamiento;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoCarrera;
import com.alvaro.enduremetrics.entity.entrenamiento.EntrenamientoGimnasio;
import com.alvaro.enduremetrics.entity.entrenamiento.VueltaCarrera;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

@Controller
public class DetalleEntrenamientoController {

    @FXML
    private Label lblDeporte;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblDistancia;
    @FXML
    private Label lblDuracion;
    @FXML
    private Label lblTss;
    @FXML
    private Label lblFrecCardiaca;
    @FXML
    private Label lblDesnivel;
    @FXML
    private Label lblPotencia;
    @FXML
    private TableView<VueltaCarrera> tablaVueltas;
    @FXML
    private TableColumn<VueltaCarrera, String> colVuelta;
    @FXML
    private TableColumn<VueltaCarrera, String> colTipo;
    @FXML
    private TableColumn<VueltaCarrera, String> colDistancia;
    @FXML
    private TableColumn<VueltaCarrera, String> colTiempo;
    @FXML
    private TableColumn<VueltaCarrera, String> colTiempoMov;
    @FXML
    private TableColumn<VueltaCarrera, String> colRitmo;
    @FXML
    private TableColumn<VueltaCarrera, String> colRitmoOptimo;
    @FXML
    private TableColumn<VueltaCarrera, String> colGap;
    @FXML
    private TableColumn<VueltaCarrera, String> colFCMin;
    @FXML
    private TableColumn<VueltaCarrera, String> colFC;
    @FXML
    private TableColumn<VueltaCarrera, String> colFCMax;
    @FXML
    private TableColumn<VueltaCarrera, String> colCalorias;
    @FXML
    private TableColumn<VueltaCarrera, String> colPotencia;
    @FXML
    private TableColumn<VueltaCarrera, String> colPotenciaMax;
    @FXML
    private TableColumn<VueltaCarrera, String> colPotenciaNP;
    @FXML
    private TableColumn<VueltaCarrera, String> colWkg;
    @FXML
    private TableColumn<VueltaCarrera, String> colCadencia;
    @FXML
    private TableColumn<VueltaCarrera, String> colCadenciaMax;
    @FXML
    private TableColumn<VueltaCarrera, String> colZancada;
    @FXML
    private TableColumn<VueltaCarrera, String> colTCS;
    @FXML
    private TableColumn<VueltaCarrera, String> colEquilibrio;
    @FXML
    private TableColumn<VueltaCarrera, String> colOscilacion;
    @FXML
    private TableColumn<VueltaCarrera, String> colRatio;
    @FXML
    private TableColumn<VueltaCarrera, String> colAscenso;
    @FXML
    private TableColumn<VueltaCarrera, String> colTemp;


    private final ApplicationContext springContext;

    // Inyectamos el contexto de Spring para cuando tengamos que volver al Calendario
    public DetalleEntrenamientoController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void cargarDatos(Entrenamiento entreno) {
        // 1. Datos Universales
        lblDeporte.setText(entreno.getClass().getSimpleName().replace("Entrenamiento", ""));

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm");
        lblFecha.setText(entreno.getFechaInicio().format(formatter));

        lblDistancia.setText(entreno.getDistancia() != null ? String.format("%.2f km", entreno.getDistancia() / 1000.0) : "-");

        if (entreno.getTiempoMovimiento() != null) {
            Duration d = Duration.ofSeconds(entreno.getTiempoMovimiento());
            lblDuracion.setText(String.format("%d:%02d:%02d", d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart()));
        }

        lblTss.setText(entreno.getCargaTss() != null ? String.valueOf(entreno.getCargaTss()) : "N/A");
        lblFrecCardiaca.setText(entreno.getFrecuenciaCardiacaMedia() != null ? entreno.getFrecuenciaCardiacaMedia() + " ppm" : "N/A");

        // 2. Datos Específicos y Tabla
        if (entreno instanceof EntrenamientoCarrera carrera) {
            lblDesnivel.setText(carrera.getDesnivelPositivo() != null ? carrera.getDesnivelPositivo() + " m" : "0 m");
            lblPotencia.setText(carrera.getPotenciaCarreraMedia() != null ? carrera.getPotenciaCarreraMedia() + " W" : "N/A");

            if (carrera.getVueltas() != null && !carrera.getVueltas().isEmpty()) {
                // Creamos la lista combinada (Vueltas + Fila Resumen)
                List<VueltaCarrera> listaParaTabla = new java.util.ArrayList<>(carrera.getVueltas());

                VueltaCarrera filaResumen = calcularFilaResumen(carrera.getVueltas());
                if (filaResumen != null) {
                    listaParaTabla.add(filaResumen);
                }

                tablaVueltas.setItems(javafx.collections.FXCollections.observableArrayList(listaParaTabla));

                // Desactivar sort para que la fila resumen no se mueva al hacer clic en cabeceras
                tablaVueltas.getColumns().forEach(column -> column.setSortable(false));
            }
        } else if (entreno instanceof EntrenamientoGimnasio gym) {
            lblDesnivel.setText("-");
            lblPotencia.setText(gym.getVolumenTotalKg() != null ? gym.getVolumenTotalKg() + " kg (Vol)" : "N/A");
            tablaVueltas.getItems().clear();
        }
    }

    @FXML
    public void initialize() {
        configurarColumnasTabla();
        tablaVueltas.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(VueltaCarrera item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || item.getTipoPaso() == null) {
                    setStyle("");
                } else if (item.getTipoPaso().equals("PROMEDIO/TOTAL")) {
                    // Fondo gris claro, texto en negrita y borde superior para separar
                    setStyle("-fx-background-color: #f8f9fa; -fx-font-weight: bold; -fx-border-color: #dee2e6 transparent transparent transparent;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configurarColumnasTabla() {
        // Formateador seguro contra nulos. Convierte a String o devuelve "-"

        colVuelta.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getNumeroVuelta() != null ? cell.getValue().getNumeroVuelta().toString() : "-"
        ));

        // Traducción semántica del tipo de vuelta
        colTipo.setCellValueFactory(cell -> {
            String tipo = cell.getValue().getTipoPaso();
            if (tipo == null) return new javafx.beans.property.SimpleStringProperty("-");
            return switch (tipo.toLowerCase()) {
                case "warmup" -> new javafx.beans.property.SimpleStringProperty("Calentamiento");
                case "active", "work" -> new javafx.beans.property.SimpleStringProperty("Trabajo");
                case "recovery" -> new javafx.beans.property.SimpleStringProperty("Descanso");
                case "cooldown" -> new javafx.beans.property.SimpleStringProperty("Enfriamiento");
                default -> new javafx.beans.property.SimpleStringProperty(tipo);
            };
        });

        colDistancia.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDistanciaMetros() != null ? String.format("%.0f", cell.getValue().getDistanciaMetros()) : "-"
        ));

        colTiempo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(formatearSegundos(cell.getValue().getTiempoSegundos())));
        colTiempoMov.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(formatearSegundos(cell.getValue().getTiempoMovimientoSegundos())));

        // Ritmos (Magia de conversión m/s -> min/km)
        colRitmo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(formatearRitmo(cell.getValue().getRitmoMedio())));
        colRitmoOptimo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(formatearRitmo(cell.getValue().getRitmoOptimo())));
        colGap.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(formatearRitmo(cell.getValue().getGapMedio())));

        colFCMin.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFrecuenciaCardiacaMinima() != null ? String.valueOf(cell.getValue().getFrecuenciaCardiacaMinima()) : "-"));
        colFC.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFrecuenciaCardiacaMedia() != null ? String.valueOf(cell.getValue().getFrecuenciaCardiacaMedia()) : "-"));
        colFCMax.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFrecuenciaCardiacaMaxima() != null ? String.valueOf(cell.getValue().getFrecuenciaCardiacaMaxima()) : "-"));
        colCalorias.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCalorias() != null ? String.valueOf(cell.getValue().getCalorias()) : "-"));


        colCadencia.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCadenciaMedia() != null ? String.valueOf(cell.getValue().getCadenciaMedia()) : "-"));
        colCadenciaMax.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCadenciaMaxima() != null ? String.valueOf(cell.getValue().getCadenciaMaxima()) : "-"));
        colZancada.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getLongitudZancadaMedia() != null ? String.format("%.2f", cell.getValue().getLongitudZancadaMedia()) : "-"));
        colTCS.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTiempoContactoSuelo() != null ? String.valueOf(cell.getValue().getTiempoContactoSuelo()) : "-"));

        colEquilibrio.setCellValueFactory(cell -> {
            Double izq = cell.getValue().getEquilibrioTcsIzquierda();
            if (izq == null) return new javafx.beans.property.SimpleStringProperty("-");
            double der = 100.0 - izq;
            return new javafx.beans.property.SimpleStringProperty(String.format("%.1f / %.1f", izq, der));
        });

        colOscilacion.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getOscilacionVertical() != null ? String.format("%.1f", cell.getValue().getOscilacionVertical()) : "-"));
        colRatio.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getRelacionVertical() != null ? String.format("%.1f", cell.getValue().getRelacionVertical()) : "-"));

        colAscenso.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getAscensoTotal() != null ? String.valueOf(cell.getValue().getAscensoTotal()) : "-"));
        colTemp.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTemperaturaMedia() != null ? String.format("%.1f", cell.getValue().getTemperaturaMedia()) : "-"));
    }


    @FXML
    public void copiarTabla(ActionEvent event) {
        // Si la tabla está vacía, no hacemos nada
        if (tablaVueltas == null || tablaVueltas.getItems().isEmpty()) {
            return;
        }

        StringBuilder clipboardString = new StringBuilder();

        // 1. Extraer las cabeceras (nombres de las columnas)
        for (TableColumn<VueltaCarrera, ?> column : tablaVueltas.getVisibleLeafColumns()) {
            clipboardString.append(column.getText()).append("\t"); // \t es el tabulador
        }
        clipboardString.append("\n");

        // 2. Extraer los datos fila por fila
        for (int i = 0; i < tablaVueltas.getItems().size(); i++) {
            for (TableColumn<VueltaCarrera, ?> column : tablaVueltas.getVisibleLeafColumns()) {
                Object cellData = column.getCellData(i);
                // Si es nulo ponemos un guion, si no, el dato real
                clipboardString.append(cellData != null ? cellData.toString() : "-").append("\t");
            }
            clipboardString.append("\n"); // Salto de línea al terminar la fila
        }

        // 3. Inyectar el texto generado en el Portapapeles de tu Sistema Operativo
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        clipboard.setContent(content);

        // 4. Feedback Visual (Magia UX para que el botón se ponga verde 2 segundos)
        Button boton = (Button) event.getSource();
        String textoOriginal = boton.getText();
        String estiloOriginal = boton.getStyle();

        boton.setText("✅ ¡Copiado!");
        boton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(2));
        pause.setOnFinished(e -> {
            boton.setText(textoOriginal);
            boton.setStyle(estiloOriginal); // Vuelve a su color normal
        });
        pause.play();
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

    private VueltaCarrera calcularFilaResumen(List<VueltaCarrera> vueltas) {
        if (vueltas == null || vueltas.isEmpty()) return null;

        VueltaCarrera res = new VueltaCarrera();
        res.setTipoPaso("PROMEDIO/TOTAL");

        // --- SUMAS ---
        double dTot = vueltas.stream().mapToDouble(VueltaCarrera::getDistanciaMetros).sum();
        int tTot = vueltas.stream().mapToInt(VueltaCarrera::getTiempoSegundos).sum();
        int kcalTot = vueltas.stream().mapToInt(v -> v.getCalorias() != null ? v.getCalorias() : 0).sum();
        double ascTot = vueltas.stream().mapToDouble(v -> v.getAscensoTotal() != null ? v.getAscensoTotal() : 0).sum();

        res.setDistanciaMetros(dTot);
        res.setTiempoSegundos(tTot);
        res.setCalorias(kcalTot);
        res.setAscensoTotal((int) ascTot);

        // --- RITMO MEDIO (Debe ser Tiempo/Distancia, no la media de los ritmos) ---
        if (dTot > 0) res.setRitmoMedio(dTot / tTot);

        // --- MEDIAS (Filtramos ceros y nulos para no falsear el promedio) ---
        res.setFrecuenciaCardiacaMedia((int) vueltas.stream().mapToInt(v -> v.getFrecuenciaCardiacaMedia() != null ? v.getFrecuenciaCardiacaMedia() : 0).filter(f -> f > 0).average().orElse(0));
        res.setCadenciaMedia((int) vueltas.stream().mapToDouble(v -> v.getCadenciaMedia() != null ? v.getCadenciaMedia() : 0).filter(c -> c > 0).average().orElse(0));
        res.setLongitudZancadaMedia(vueltas.stream().mapToDouble(v -> v.getLongitudZancadaMedia() != null ? v.getLongitudZancadaMedia() : 0).filter(z -> z > 0).average().orElse(0));
        res.setTiempoContactoSuelo((int) vueltas.stream().mapToDouble(v -> v.getTiempoContactoSuelo() != null ? v.getTiempoContactoSuelo() : 0).filter(t -> t > 0).average().orElse(0));
        res.setOscilacionVertical(vueltas.stream().mapToDouble(v -> v.getOscilacionVertical() != null ? v.getOscilacionVertical() : 0).filter(o -> o > 0).average().orElse(0));
        res.setEquilibrioTcsIzquierda(vueltas.stream().mapToDouble(v -> v.getEquilibrioTcsIzquierda() != null ? v.getEquilibrioTcsIzquierda() : 0).filter(e -> e > 0).average().orElse(0));

        // --- RATIO VERTICAL % (Calculado sobre las medias) ---
        if (res.getLongitudZancadaMedia() > 0 && res.getOscilacionVertical() > 0) {
            double voMetros = res.getOscilacionVertical() / 100.0; // cm a metros
            res.setRelacionVertical((voMetros / res.getLongitudZancadaMedia()) * 10);
        }

        return res;
    }


    // Transforma m/s de Intervals a formato min/km clásico
    private String formatearRitmo(Double metrosPorSegundo) {
        if (metrosPorSegundo == null || metrosPorSegundo <= 0) return "-";
        double minPorKm = 16.666666666667 / metrosPorSegundo;
        int minutos = (int) minPorKm;
        int segundos = (int) Math.round((minPorKm - minutos) * 60);
        if (segundos == 60) {
            minutos++;
            segundos = 0;
        }
        return String.format("%d:%02d", minutos, segundos);
    }

    // Pasa de 315 segundos a "5:15"
    private String formatearSegundos(Integer segundosTotales) {
        if (segundosTotales == null) return "-";
        int horas = segundosTotales / 3600;
        int minutos = (segundosTotales % 3600) / 60;
        int segundos = segundosTotales % 60;

        if (horas > 0) {
            return String.format("%d:%02d:%02d", horas, minutos, segundos);
        } else {
            return String.format("%d:%02d", minutos, segundos);
        }
    }
}