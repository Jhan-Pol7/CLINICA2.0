package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.components.ColumnInfo;
import pe.edu.upeu.clinica.components.TableViewHelper;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.service.ICitaService;

import java.time.LocalDate;
import java.util.LinkedHashMap;

/**
 * Recepción: confirmar llegada (PROGRAMADA → EN_ESPERA) o cancelar cita.
 * El click "Editar" del helper hace check-in y el "Eliminar" cancela (con confirmación).
 */
public class MainCheckinController {

    private final ICitaService citaService;
    private final ObservableList<Cita> data = FXCollections.observableArrayList();

    @FXML private DatePicker dpFecha;
    @FXML private TableView<Cita> tabla;

    public MainCheckinController(ICitaService citaService) { this.citaService = citaService; }

    @FXML
    public void initialize() {
        dpFecha.setValue(LocalDate.now());

        TableViewHelper<Cita> h = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> cols = new LinkedHashMap<>();
        cols.put("Ticket",       new ColumnInfo("numTicket",            130.0));
        cols.put("Paciente",     new ColumnInfo("paciente.nombres",     140.0));
        cols.put("DNI",          new ColumnInfo("paciente.dni",         100.0));
        cols.put("Médico",       new ColumnInfo("medico.nombres",       130.0));
        cols.put("Especialidad", new ColumnInfo("especialidad.nombre",  130.0));
        cols.put("Hora",         new ColumnInfo("hora",                  80.0));
        cols.put("Estado",       new ColumnInfo("estado",               110.0));
        h.addColumnsInOrderWithSize(tabla, cols, this::confirmarLlegada, this::cancelarConfirmar);
        recargar();
    }

    @FXML
    public void onRecargar() { recargar(); }

    // Recarga la tabla con citas PROGRAMADAS del día seleccionado.
    private void recargar() {
        LocalDate f = dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue();
        data.setAll(citaService.findByEstadoYFecha(EstadoCita.PROGRAMADA, f));
        tabla.setItems(data);
    }

    // Confirma la llegada del paciente — cambia el estado de PROGRAMADA a EN_ESPERA.
    private void confirmarLlegada(Cita c) {
        try {
            citaService.checkIn(c.getIdCita());
            mostrarExito("Check-in OK — " + c.getNumTicket() + " ahora EN_ESPERA");
            recargar();
        } catch (Exception ex) { mostrarError(ex.getMessage()); }
    }

    // Cancela la cita con diálogo de confirmación previo.
    private void cancelarConfirmar(Cita c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cancelar la cita " + c.getNumTicket() + " de " + c.getPaciente().getNombres() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("Confirmar cancelación");
        a.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try { citaService.cancelar(c.getIdCita()); mostrarExito("Cita cancelada"); recargar(); }
            catch (Exception ex) { mostrarError(ex.getMessage()); }
        });
    }

    private Stage stage() { return tabla == null || tabla.getScene() == null ? null : (Stage) tabla.getScene().getWindow(); }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 3000, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2200, s.getX() + 80, s.getY() + 80); }
}
