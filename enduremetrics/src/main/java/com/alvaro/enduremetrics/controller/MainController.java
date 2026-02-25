package com.alvaro.enduremetrics.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import com.alvaro.enduremetrics.session.UserSession;

import java.io.IOException;
import javafx.scene.layout.AnchorPane;

@Controller
public class MainController {

    private final UserSession userSession;
    private final ApplicationContext springContext;

    @FXML
    private StackPane panelCentral;

    // Ahora capturamos los dos elementos del menú
    @FXML
    private AnchorPane menuContenedor;
    @FXML
    private VBox menuLateral;

    private boolean menuAbierto = true;

    public MainController(UserSession userSession, ApplicationContext springContext) {
        this.userSession = userSession;
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        cargarVista("/views/dashboard-view.fxml");
    }

    // ==========================================
    // MENÚ HAMBURGUESA (Efecto Deslizante Fluido)
    // ==========================================
    @FXML
    public void toggleMenu() {
        menuAbierto = !menuAbierto;

        if (menuAbierto) {
            menuContenedor.setVisible(true);
            menuContenedor.setManaged(true);
        }

        Timeline timeline = new Timeline();

        // Si se abre, el ancho es 250 y la posición X es 0.
        // Si se cierra, el ancho es 0 y lo desplazamos -250px a la izquierda.
        double targetWidth = menuAbierto ? 250.0 : 0.0;
        double targetTranslate = menuAbierto ? 0.0 : -250.0;

        // Animación 1: Encoger la caja contenedora para que el centro se expanda
        // suavemente
        KeyValue kvWidth = new KeyValue(menuContenedor.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);

        // Animación 2: Deslizar físicamente el contenido del menú hacia afuera
        KeyValue kvTranslate = new KeyValue(menuLateral.translateXProperty(), targetTranslate, Interpolator.EASE_BOTH);

        // Agrupamos ambas animaciones en un fotograma de 300 milisegundos
        KeyFrame kf = new KeyFrame(Duration.millis(300), kvWidth, kvTranslate);
        timeline.getKeyFrames().add(kf);

        if (!menuAbierto) {
            timeline.setOnFinished(e -> {
                menuContenedor.setVisible(false);
                menuContenedor.setManaged(false);
            });
        }

        timeline.play();
    }

    @FXML
    public void mostrarDashboard() {
        // Enlaza este método al onAction del botón Dashboard en el menú
        cargarVista("/views/dashboard-view.fxml");
    }

    @FXML
    public void cerrarSesion() {
        userSession.cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) panelCentral.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void mostrarPerfil() {
        cargarVista("/views/profile-view.fxml");
    }

    private void cargarVista(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            loader.setControllerFactory(springContext::getBean);
            Parent vista = loader.load();
            panelCentral.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}