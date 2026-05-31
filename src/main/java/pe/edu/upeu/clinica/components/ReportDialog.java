package pe.edu.upeu.clinica.components;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import net.sf.jasperreports.engine.JasperPrint;
import win.zqxu.jrviewer.JRViewerFX;

// Alternativa a ReportAlert: usa un Dialog redimensionable en lugar de Alert.
// Permite mayor tamaño (940 × 640 px predeterminado) y coloca el botón
// "Cerrar" dentro del contenido (no en la barra de botones del diálogo),
// lo que da más espacio al visor del reporte.
// Preferible para reportes de varias páginas o con mucho detalle.
public class ReportDialog {
    private final JasperPrint jasperPrint;
    public ReportDialog(JasperPrint jasperPrint) { this.jasperPrint = jasperPrint; }

    // Construye el Dialog, incrusta el JRViewerFX (900 × 600) junto con
    // un botón "Cerrar" y muestra el conjunto redimensionable a 940 × 640.
    // Captura cualquier excepción del visor para no romper la aplicación.
    public void show() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Visualizar Reporte");
        dialog.setHeaderText(null);
        try {
            JRViewerFX viewerFX = new JRViewerFX(jasperPrint);
            viewerFX.setPrefSize(900, 600);
            VBox vbox = new VBox(viewerFX);
            vbox.setSpacing(10);
            Button closeButton = new Button("Cerrar");
            closeButton.setOnAction(e -> dialog.close());
            vbox.getChildren().add(closeButton);
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(vbox);
            dialog.setResizable(true);
            dialog.getDialogPane().setContent(dialogPane);
            dialog.setHeight(640);
            dialog.setWidth(940);
            dialog.showAndWait();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
