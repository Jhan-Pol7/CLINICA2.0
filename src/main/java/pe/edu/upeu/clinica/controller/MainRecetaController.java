package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import pe.edu.upeu.clinica.components.JasperViewerFX;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.dto.SessionManager;
import pe.edu.upeu.clinica.model.Consulta;
import pe.edu.upeu.clinica.model.Receta;
import pe.edu.upeu.clinica.model.RecetaDetalle;
import pe.edu.upeu.clinica.service.IRecetaService;
import pe.edu.upeu.clinica.service.IReporteService;

import java.io.File;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Visualización de la última receta cerrada por el médico.
 * Cabecera con fecha + indicaciones + recomendaciones, tabla de medicamentos.
 */
public class MainRecetaController {

    private final IRecetaService recetaService;
    private final IReporteService reporteService;
    private final ObservableList<RecetaDetalle> detalles = FXCollections.observableArrayList();

    @FXML private Label lblTitulo, lblCabecera, lblFecha;
    @FXML private TextArea txtIndicaciones, txtRecomendaciones;
    @FXML private TableView<RecetaDetalle> tabla;

    public MainRecetaController(IRecetaService recetaService, IReporteService reporteService) {
        this.recetaService = recetaService;
        this.reporteService = reporteService;
    }

    @FXML
    public void initialize() {
        TableColumn<RecetaDetalle, String> cMed   = new TableColumn<>("Medicamento");
        TableColumn<RecetaDetalle, String> cDosis = new TableColumn<>("Dosis");
        TableColumn<RecetaDetalle, String> cFrec  = new TableColumn<>("Frecuencia");
        TableColumn<RecetaDetalle, String> cDur   = new TableColumn<>("Duración");
        TableColumn<RecetaDetalle, String> cVia   = new TableColumn<>("Vía");
        cMed.setCellValueFactory(new PropertyValueFactory<>("medicamento")); cMed.setPrefWidth(220);
        cDosis.setCellValueFactory(new PropertyValueFactory<>("dosis"));     cDosis.setPrefWidth(100);
        cFrec.setCellValueFactory(new PropertyValueFactory<>("frecuencia")); cFrec.setPrefWidth(130);
        cDur.setCellValueFactory(new PropertyValueFactory<>("duracion"));    cDur.setPrefWidth(110);
        cVia.setCellValueFactory(new PropertyValueFactory<>("via"));         cVia.setPrefWidth(90);
        tabla.getColumns().setAll(cMed, cDosis, cFrec, cDur, cVia);
        tabla.setItems(detalles);

        if (txtIndicaciones != null)   txtIndicaciones.setFont(Font.font("Segoe UI", 13));
        if (txtRecomendaciones != null) txtRecomendaciones.setFont(Font.font("Segoe UI", 13));

        renderizar();
    }

    @FXML
    public void onRecargar() { renderizar(); }

    // Renderiza la última receta de la consulta guardada en sesión.
    // Si no hay consulta o no tiene receta, muestra mensajes informativos.
    private void renderizar() {
        detalles.clear();
        Consulta last = SessionManager.getInstance().getLastConsulta();
        if (last == null) {
            if (lblTitulo != null) lblTitulo.setText("Sin receta reciente");
            if (lblCabecera != null) lblCabecera.setText("Finaliza una consulta médica para ver la receta aquí.");
            if (lblFecha != null) lblFecha.setText("");
            if (txtIndicaciones != null) txtIndicaciones.clear();
            if (txtRecomendaciones != null) txtRecomendaciones.clear();
            return;
        }
        Optional<Receta> r = recetaService.findByConsulta(last.getIdConsulta());
        if (r.isEmpty()) {
            if (lblTitulo != null) lblTitulo.setText("Consulta sin receta");
            if (lblCabecera != null) lblCabecera.setText("La consulta fue cerrada sin medicación prescrita.");
            return;
        }
        Receta receta = r.get();
        if (lblTitulo != null)   lblTitulo.setText("Receta médica #" + receta.getIdReceta());
        if (lblCabecera != null) lblCabecera.setText("Diagnóstico: " + (last.getDiagnostico() == null ? "—" : last.getDiagnostico()));
        if (lblFecha != null)    lblFecha.setText("Fecha: " + (receta.getFechaReg() == null ? "—"
                : receta.getFechaReg().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        if (txtIndicaciones != null)   txtIndicaciones.setText(receta.getIndicacionesGenerales() == null ? "" : receta.getIndicacionesGenerales());
        if (txtRecomendaciones != null) txtRecomendaciones.setText(receta.getRecomendaciones() == null ? "" : receta.getRecomendaciones());
        List<RecetaDetalle> d = recetaService.findDetalles(receta.getIdReceta());
        detalles.setAll(d);
    }

    @FXML
    // Copia al portapapeles el contenido completo de la receta en texto plano.
    public void onCopiar() {
        StringBuilder sb = new StringBuilder();
        sb.append(lblTitulo == null ? "" : lblTitulo.getText()).append("\n");
        sb.append(lblCabecera == null ? "" : lblCabecera.getText()).append("\n");
        sb.append(lblFecha == null ? "" : lblFecha.getText()).append("\n\n");
        sb.append("Indicaciones generales:\n").append(txtIndicaciones == null ? "" : txtIndicaciones.getText()).append("\n\n");
        sb.append("Recomendaciones:\n").append(txtRecomendaciones == null ? "" : txtRecomendaciones.getText()).append("\n\n");
        sb.append("Medicamentos:\n");
        for (RecetaDetalle d : detalles) {
            sb.append(" • ").append(d.getMedicamento())
                    .append(" — ").append(d.getDosis())
                    .append(" | ").append(d.getFrecuencia())
                    .append(" | ").append(d.getDuracion())
                    .append(" | ").append(d.getVia()).append("\n");
        }
        ClipboardContent c = new ClipboardContent();
        c.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(c);
        Stage s = stage();
        if (s != null) Toast.showSuccess(s, "Receta copiada", 1800, s.getX() + 80, s.getY() + 80);
    }

    @FXML
    // Abre el visor Jasper con el reporte de la receta.
    public void onVisorJasper() {
        Consulta last = SessionManager.getInstance().getLastConsulta();
        if (last == null) { mostrarError("No hay receta reciente"); return; }
        try {
            JasperPrint jp = reporteService.generarReceta(last);
            new JasperViewerFX().viewReport("Receta médica", jp);
        } catch (Exception ex) { mostrarError("Error Jasper: " + ex.getMessage()); }
    }

    @FXML
    // Exporta la receta como archivo PDF usando JasperReports.
    public void onExportarPdf() {
        Consulta last = SessionManager.getInstance().getLastConsulta();
        if (last == null) { mostrarError("No hay receta"); return; }
        try {
            JasperPrint jp = reporteService.generarReceta(last);
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar receta como PDF");
            fc.setInitialFileName("receta_" + last.getIdConsulta() + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showSaveDialog(stage());
            if (f == null) return;
            JasperExportManager.exportReportToPdfFile(jp, f.getAbsolutePath());
            Stage s = stage();
            if (s != null) Toast.showSuccess(s, "PDF guardado: " + f.getName(), 2200, s.getX() + 80, s.getY() + 80);
        } catch (Exception ex) { mostrarError("Error PDF: " + ex.getMessage()); }
    }

    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 3000, s.getX() + 80, s.getY() + 80); }
    private Stage stage() { return tabla == null || tabla.getScene() == null ? null : (Stage) tabla.getScene().getWindow(); }
}
