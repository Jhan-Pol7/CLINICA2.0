package pe.edu.upeu.clinica.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import net.sf.jasperreports.engine.JasperPrint;
import win.zqxu.jrviewer.JRViewerFX;

// Muestra un reporte JasperReports dentro de un Alert modal de JavaFX.
// Envuelve JRViewerFX (visor de JasperReports) en un Alert de tipo NONE
// para reutilizar el ciclo de vida del diálogo sin crear una Stage aparte.
// Usar cuando el reporte es sencillo y no requiere mucho espacio vertical.
public class ReportAlert {
    private final JasperPrint jasperPrint;
    public ReportAlert(JasperPrint jasperPrint) { this.jasperPrint = jasperPrint; }

    // Construye el Alert, incrusta el visor Jasper (900 × 500 px) y espera
    // bloqueando hasta que el usuario presione "Cerrar" o cierre la ventana.
    public void show() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Visualizar Reporte");
        alert.setHeaderText(null);
        JRViewerFX viewerFX = new JRViewerFX(jasperPrint);
        viewerFX.setPrefSize(900, 500);
        StackPane stackPane = new StackPane(viewerFX);
        alert.getDialogPane().setContent(stackPane);
        ButtonType closeButton = new ButtonType("Cerrar");
        alert.getButtonTypes().add(closeButton);
        alert.setOnCloseRequest(event -> alert.close());
        alert.showAndWait();
    }
}
