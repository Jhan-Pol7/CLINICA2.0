package pe.edu.upeu.clinica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.config.AppContext;
import pe.edu.upeu.clinica.config.DatabaseConfig;

import java.net.URL;

/**
 * Bootstrap JavaFX. Inicializa la BD, arma el AppContext (DI manual)
 * y carga la ventana de login.
 */
public class ClinicaApplication extends Application {
    private Parent parent;

    @Override
    public void init() throws Exception {
        DatabaseConfig.init();                    // pool + schema H2
        AppContext context = AppContext.getInstance(); // contenedor DI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        loader.setControllerFactory(context::getBean);
        parent = loader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("Clinica2.0 — Más Cerca de Dios");
        URL icon = getClass().getResource("/img/logo_clinica.png");
        if (icon != null) {
            stage.getIcons().add(new Image(icon.toExternalForm()));
        }
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        DatabaseConfig.shutdown();
    }
}
