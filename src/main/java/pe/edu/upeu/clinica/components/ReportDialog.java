package pe.edu.upeu.clinica.components;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import net.sf.jasperreports.engine.JasperPrint;
import win.zqxu.jrviewer.JRViewerFX;

public class ReportDialog {
    private final JasperPrint jasperPrint;
    public ReportDialog(JasperPrint jasperPrint) { this.jasperPrint = jasperPrint; }

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
