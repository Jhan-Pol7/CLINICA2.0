package pe.edu.upeu.clinica.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.components.StageManager;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.config.AppContext;
import pe.edu.upeu.clinica.dto.SessionManager;
import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.service.IUsuarioService;

import java.io.IOException;
import java.net.URL;

// Controller del FXML de login. Valida credenciales contra UsuarioService;
// si son correctas pobla SessionManager y cambia la Scene actual al maingui.fxml
// reaprovechando el mismo Stage (no abre una nueva ventana).
public class LoginController {

    // Servicio inyectado por AppContext.
    private final IUsuarioService us;

    public LoginController(IUsuarioService us) { this.us = us; }

    // Campos FXML — los nombres deben coincidir con fx:id en login.fxml.
    @FXML TextField txtUsuario;
    @FXML PasswordField txtClave;
    @FXML Button btnIngresar;

    @FXML
    public void initialize() {
        // Atajo de teclado: Enter en el campo de clave dispara "Ingresar".
        if (txtClave != null) {
            txtClave.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER && btnIngresar != null) {
                    btnIngresar.fire();
                }
            });
        }
    }

    // Acción del botón "Ingresar" (onAction="#login" en el FXML).
    @FXML
    public void login(ActionEvent event) {
        try {
            // Optional.ifPresentOrElse: si el login devuelve usuario -> abrirMain;
            // si está vacío -> Toast de error.
            us.loginUsuario(txtUsuario.getText(), txtClave.getText())
                    .ifPresentOrElse(
                            usu -> abrirMain(event, usu),
                            () -> mostrarError(event));
        } catch (Exception e) {
            // Cualquier excepción inesperada (BD caída, etc.) se loguea pero no rompe la UI.
            System.err.println("Error en login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Pobla la sesión y cambia la escena al maingui.
    private void abrirMain(ActionEvent event, Usuario usu) {
        try {
            // Guardar info del logueado en el singleton SessionManager.
            SessionManager s = SessionManager.getInstance();
            s.setUserId(usu.getIdUsuario());
            s.setUserName(usu.getUsuario());
            s.setUserPerfil(usu.getIdPerfil().getNombre());
            s.setIdReferencia(usu.getIdReferencia());  // id_medico o id_enfermero si aplica

            // Cargar maingui.fxml usando el AppContext como factory de controllers
            // (para que MainGuiController reciba IMenuMenuItemDao por constructor).
            AppContext ctx = AppContext.getInstance();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/maingui.fxml"));
            loader.setControllerFactory(ctx::getBean);
            Parent mainRoot = loader.load();

            // Ventana a pantalla completa (menos la barra de tareas).
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getBounds();
            Scene mainScene = new Scene(mainRoot, bounds.getWidth(), bounds.getHeight() - 30);

            // Aplicar CSS por defecto (si existe).
            URL css = getClass().getResource("/css/styles.css");
            if (css != null) mainScene.getStylesheets().add(css.toExternalForm());

            // Reusar el mismo Stage en vez de abrir uno nuevo.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            URL icon = getClass().getResource("/img/logo_clinica.png");
            if (icon != null) {
                stage.getIcons().clear();
                stage.getIcons().add(new Image(icon.toExternalForm()));
            }
            stage.setScene(mainScene);
            stage.setTitle("Clinica2.0 — Más Cerca de Dios");
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setResizable(true);
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            // Guardar referencia al stage principal para que otros controllers
            // puedan obtenerlo sin pasar el Stage por parámetro.
            StageManager.setPrimaryStage(stage);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error abriendo ventana principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Toast rojo cerca del botón "Ingresar".
    private void mostrarError(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double w = stage.getX() + 60;
        double h = stage.getY() + 60;
        Toast.showToast(stage, "Credenciales inválidas. Intente nuevamente", 2200, w, h);
    }

    // Acción del botón "Cerrar" (onAction="#cerrar"). Cierra la app limpiamente.
    @FXML
    public void cerrar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }
}
