package com.alvaro.enduremetrics.controller;

import com.alvaro.enduremetrics.dto.ProfileDTO;
import com.alvaro.enduremetrics.entity.Zapatilla;
import com.alvaro.enduremetrics.repository.ZapatillasRepository;
import com.alvaro.enduremetrics.service.ProfileService;
import com.alvaro.enduremetrics.service.ZapatillasService;
import com.alvaro.enduremetrics.session.UserSession;
import com.alvaro.enduremetrics.util.ViewUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Controller
public class ProfileController {

    // 1. Dependencias inyectadas por constructor (Obligatorio para código limpio)
    private final UserSession userSession;
    private final ProfileService profileService;
    private final ApplicationContext springContext;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField pesoField;
    @FXML
    private TextField alturaField;
    @FXML
    private DatePicker fechaNacimientoPicker;
    @FXML
    private Label mensajeLabel;
    @FXML
    private ComboBox<String> sexoComboBox;
    @FXML
    private TableColumn<Zapatilla, String> colMarcaModelo;
    @FXML
    private TableColumn<Zapatilla, Double> colKm;
    @FXML
    private TableColumn<Zapatilla, String> colEstado;
    @FXML
    private TableView<Zapatilla> zapatillasTable;
    @FXML
    private Button btnEliminarZapatilla;
    @FXML
    private TableColumn<Zapatilla, Double> colKmMaximos;
    private final ZapatillasService zapatillasService;
    private final ZapatillasRepository zapatillasRepository;

    public ProfileController(UserSession userSession, ProfileService profileService, ApplicationContext springContext, ZapatillasService zapatillasService, ZapatillasRepository zapatillasRepository) {
        this.userSession = userSession;
        this.profileService = profileService;
        this.springContext = springContext;
        this.zapatillasService = zapatillasService;

        this.zapatillasRepository = zapatillasRepository;
    }

    @FXML
    public void initialize() {
        configurarDesplegables();
        cargarDatosUsuario();
        configurarColumnasZapatillas();
        cargarZapatillas();
        btnEliminarZapatilla.disableProperty().bind(zapatillasTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void cargarDatosUsuario() {
        // 2. Early return: Si no hay sesión, no hacemos nada y salimos.
        if (!userSession.haySesionActiva()) return;

        ProfileDTO perfil = profileService.obtenerPerfil(userSession.getUsuarioLogueado());

        usernameField.setText(perfil.username());

        if (perfil.peso() != null) pesoField.setText(perfil.peso().toString());
        if (perfil.altura() != null) alturaField.setText(perfil.altura().toString());
        if (perfil.fechaNacimiento() != null) fechaNacimientoPicker.setValue(perfil.fechaNacimiento());
        if (perfil.sexo() != null) sexoComboBox.setValue(perfil.sexo());
    }

    @FXML
    public void guardarPerfil() {
        if (!userSession.haySesionActiva()) return;

        try {
            // Parseamos los Strings a Integer de forma segura
            Double peso = parsearNumero(pesoField.getText(), Double::valueOf);
            Integer altura = parsearNumero(alturaField.getText(), Integer::valueOf);
            LocalDate fechaNac = fechaNacimientoPicker.getValue();
            String sexo = sexoComboBox.getValue();

            // Montamos el DTO con los datos limpios (Asegúrate de tener estos campos en tu ProfileDTO)
            ProfileDTO datosActualizados = new ProfileDTO(userSession.getUsuarioLogueado().getUsername(), altura, fechaNac, sexo, peso);

            // Delegamos el guardado en la base de datos a la capa de servicio
            profileService.actualizarPerfil(userSession.getUsuarioLogueado(), datosActualizados);

            // Asumiendo que has creado este método en ViewUtils para cambiar textos de Labels
            ViewUtils.mostrarMensaje(mensajeLabel, "Perfil actualizado correctamente", "#27ae60");

        } catch (NumberFormatException e) {
            ViewUtils.mostrarMensaje(mensajeLabel, "Revisa los campos numéricos (peso, altura, fcm).", "#e74c3c");
        } catch (Exception e) {
            ViewUtils.mostrarMensaje(mensajeLabel, "Error al guardar el perfil.", "#e74c3c");
        }
    }

    @FXML
    public void abrirModalNuevaZapatilla() {
        try {
            // 1. Cargamos el FXML de la ventanita
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/zapatilla-modal.fxml"));

            // 2. ¡VITAL! Le decimos a Spring que inyecte las dependencias del nuevo controlador
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // 3. Creamos un nuevo "Escenario" (Ventana)
            Stage modalStage = new Stage();
            modalStage.setTitle("Añadir Zapatilla");
            modalStage.setScene(new Scene(root));

            // 4. La magia del Modal: Bloquea la ventana principal hasta que esta se cierre
            modalStage.initModality(Modality.APPLICATION_MODAL);

            // Opcional: Evitar que el usuario redimensione la ventanita
            modalStage.setResizable(false);

            // 5. Mostrar la ventana y pausar la ejecución aquí hasta que el usuario la cierre
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            ViewUtils.mostrarMensaje(mensajeLabel, "Error al abrir la ventana de zapatillas.", "#e74c3c");
        }
    }

    private void configurarDesplegables() {
        ViewUtils.popularComboBox(sexoComboBox, "Hombre", "Mujer", "Otro", "Prefiero no decirlo");
    }

    // 3. Método Helper para evitar NullPointerExceptions y manejar campos vacíos
    private <T> T parsearNumero(String texto, Function<String, T> conversor) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String textoLimpio = texto.trim().replace(",", ".");
        return conversor.apply(textoLimpio);
    }

    private void configurarColumnasZapatillas() {
        colMarcaModelo.setCellValueFactory(cellData -> {
            Zapatilla z = cellData.getValue();
            return new SimpleStringProperty(z.getMarca() + " " + z.getModelo());
        });

        colKm.setCellValueFactory(new PropertyValueFactory<>("kmActuales"));

        colEstado.setCellValueFactory(cellData -> {
            boolean activa = cellData.getValue().getActiva(); // O isActiva() según tu getter
            return new SimpleStringProperty(activa ? "Activa" : "Retirada");
        });

        colKmMaximos.setCellValueFactory(new PropertyValueFactory<>("kmMaximos"));
    }

    public void cargarZapatillas() {
        if (!userSession.haySesionActiva()) return;

        // 1. Pedimos la lista normal de Java a tu servicio y PostgreSQL
        List<Zapatilla> listaZapatillas = zapatillasService.obtenerZapatillas(userSession.getUsuarioLogueado());

        // 2. La convertimos en una lista reactiva de JavaFX
        ObservableList<Zapatilla> datosReactivos = FXCollections.observableArrayList(listaZapatillas);

        // 3. Se la inyectamos a la tabla
        zapatillasTable.setItems(datosReactivos);
    }

    @FXML
    public void eliminarZapatilla() {
        // 1. Obtenemos el objeto Zapatilla de la fila seleccionada
        Zapatilla zapatillaSeleccionada = zapatillasTable.getSelectionModel().getSelectedItem();

        if (zapatillaSeleccionada == null) return; // Seguridad extra

        // 2. UX: Pedimos confirmación antes de destruir datos
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Eliminar Zapatilla");
        alerta.setHeaderText("Vas a eliminar: " + zapatillaSeleccionada.getMarca() + " " + zapatillaSeleccionada.getModelo());
        alerta.setContentText("¿Estás seguro? Esta acción no se puede deshacer.");

        // 3. Si el usuario pulsa "Aceptar", ejecutamos el borrado
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                zapatillasService.eliminarZapatilla(zapatillaSeleccionada);
                cargarZapatillas(); // Recargamos la tabla automáticamente para que desaparezca
            }
        });
    }


}