package pe.edu.upeu.clinica.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Ticket;
import pe.edu.upeu.clinica.service.IReporteService;
import pe.edu.upeu.clinica.service.ITicketService;
import pe.edu.upeu.clinica.utils.TicketPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Muestra el ticket de la última cita registrada (SessionManager.lastCita).
 * En Fase 3 se renderiza como texto monoespaciado con el layout requerido.
 * Botones: Copiar al portapapeles, Guardar como .txt, Imprimir (ESC/POS).
 * El visor Jasper se añade en Fase 4 (.jrxml).
 */
public class MainTicketController {

    private final ITicketService ticketService;
    private final IReporteService reporteService;
    private Ticket ticket;

    @FXML private Label lblTitulo;
    @FXML private TextArea txtTicket;

    public MainTicketController(ITicketService ticketService, IReporteService reporteService) {
        this.ticketService = ticketService;
        this.reporteService = reporteService;
    }

    @FXML
    public void initialize() {
        txtTicket.setFont(Font.font("Consolas", 13));
        renderizar();
    }

    @FXML
    public void onRecargar() { renderizar(); }

    // Renderiza el ticket de la última cita guardada en sesión.
    // Usa el servicio para construir el DTO Ticket y lo muestra como texto monoespaciado.
    private void renderizar() {
        Cita cita = SessionManager.getInstance().getLastCita();
        if (cita == null) {
            txtTicket.setText("No hay ticket reciente para mostrar.\n\n"
                    + "Registra primero una cita en \"Atención → Registrar Cita\".");
            if (lblTitulo != null) lblTitulo.setText("Sin ticket");
            ticket = null;
            return;
        }
        ticket = ticketService.buildTicket(cita);
        txtTicket.setText(ticketService.renderText(ticket));
        if (lblTitulo != null) lblTitulo.setText("Ticket " + ticket.getNumTicket());
    }

    @FXML
    // Copia el texto del ticket al portapapeles del sistema.
    public void onCopiar() {
        if (txtTicket.getText().isEmpty()) return;
        ClipboardContent c = new ClipboardContent();
        c.putString(txtTicket.getText());
        Clipboard.getSystemClipboard().setContent(c);
        mostrarExito("Ticket copiado al portapapeles");
    }

    @FXML
    // Guarda el ticket como archivo .txt usando FileChooser.
    public void onGuardarTxt() {
        if (ticket == null) { mostrarError("No hay ticket que guardar"); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar ticket");
        fc.setInitialFileName(ticket.getNumTicket() + ".txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto", "*.txt"));
        File f = fc.showSaveDialog(stage());
        if (f == null) return;
        try {
            Files.writeString(f.toPath(), txtTicket.getText(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            mostrarExito("Guardado: " + f.getName());
        } catch (IOException ex) { mostrarError("Error al guardar: " + ex.getMessage()); }
    }

    @FXML
    // Envía el ticket a la impresora térmica ESC/POS.
    public void onImprimir() {
        if (ticket == null) { mostrarError("No hay ticket"); return; }
        try {
            new TicketPrinter().imprimir(ticket);
            mostrarExito("Ticket enviado a la impresora térmica");
        } catch (IOException ex) {
            mostrarError("Sin impresora ESC/POS — usa 'Visor Jasper' o 'PDF' como alternativa");
        }
    }

    @FXML
    // Abre una ventana con el visor de JasperReports para previsualizar el ticket.
    public void onVisorJasper() {
        Cita cita = SessionManager.getInstance().getLastCita();
        if (cita == null) { mostrarError("No hay ticket"); return; }
        try {
            JasperPrint jp = reporteService.generarTicket(cita);
            new JasperViewerFX().viewReport("Ticket " + (ticket == null ? "" : ticket.getNumTicket()), jp);
        } catch (Exception ex) { mostrarError("Error en Jasper: " + ex.getMessage()); }
    }

    @FXML
    // Exporta el ticket como archivo PDF mediante JasperReports.
    public void onExportarPdf() {
        Cita cita = SessionManager.getInstance().getLastCita();
        if (cita == null) { mostrarError("No hay ticket"); return; }
        try {
            JasperPrint jp = reporteService.generarTicket(cita);
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar ticket en PDF");
            fc.setInitialFileName((ticket == null ? "ticket" : ticket.getNumTicket()) + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showSaveDialog(stage());
            if (f == null) return;
            JasperExportManager.exportReportToPdfFile(jp, f.getAbsolutePath());
            mostrarExito("PDF guardado: " + f.getName());
        } catch (Exception ex) { mostrarError("Error PDF: " + ex.getMessage()); }
    }

    private Stage stage() { return txtTicket == null || txtTicket.getScene() == null ? null : (Stage) txtTicket.getScene().getWindow(); }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 3000, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2200, s.getX() + 80, s.getY() + 80); }
}
