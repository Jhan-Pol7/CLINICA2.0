package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.components.ColumnInfo;
import pe.edu.upeu.clinica.components.TableViewHelper;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.enums.DiaSemana;
import pe.edu.upeu.clinica.model.Horario;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.service.IHorarioService;
import pe.edu.upeu.clinica.service.IMedicoService;

import java.time.LocalTime;

import java.util.LinkedHashMap;

// CRUD de Horario. Recibe IMedicoService para poblar el combo de médicos.
// La hora se ingresa como texto "HH:mm" (DatePicker no aplica a horas en JavaFX);
// se parsea con LocalTime.parse y se valida que fin > inicio.
public class MainHorarioController {

    private final IHorarioService service;
    private final IMedicoService medicoService;        // para alimentar cmbMedico
    private final ObservableList<Horario> data = FXCollections.observableArrayList();
    private Horario seleccionado;

    @FXML private ComboBox<Medico> cmbMedico;
    @FXML private ComboBox<DiaSemana> cmbDia;
    @FXML private Spinner<Integer> spnInicioH, spnInicioM;  // Spinners hora inicio (HH:mm)
    @FXML private Spinner<Integer> spnFinH, spnFinM;          // Spinners hora fin (HH:mm)
    @FXML private TableView<Horario> tabla;

    public MainHorarioController(IHorarioService service, IMedicoService medicoService) {
        this.service = service;
        this.medicoService = medicoService;
    }

    @FXML
    public void initialize() {
        cmbMedico.setItems(FXCollections.observableArrayList(medicoService.findAll()));
        cmbDia.setItems(FXCollections.observableArrayList(DiaSemana.values()));

        // Configurar Spinners de hora con wrapping circular.
        spnInicioH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8, 1));
        spnInicioM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        spnFinH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 13, 1));
        spnFinM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        spnInicioH.getValueFactory().setWrapAround(true);
        spnInicioM.getValueFactory().setWrapAround(true);
        spnFinH.getValueFactory().setWrapAround(true);
        spnFinM.getValueFactory().setWrapAround(true);

        TableViewHelper<Horario> h = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> cols = new LinkedHashMap<>();
        cols.put("ID",          new ColumnInfo("idHorario",       60.0));
        cols.put("Médico",      new ColumnInfo("medico.nombres", 200.0));
        cols.put("Día",         new ColumnInfo("diaSemana",       70.0));
        cols.put("Inicio",      new ColumnInfo("horaInicio",      80.0));
        cols.put("Fin",         new ColumnInfo("horaFin",         80.0));
        h.addColumnsInOrderWithSize(tabla, cols, this::editar, this::confirmarEliminar);
        cargar();
    }

    private void cargar() { data.setAll(service.findAll()); tabla.setItems(data); }

    @FXML public void onNuevo()   { seleccionado = null; onLimpiar(); }
    @FXML public void onLimpiar() {
        cmbMedico.getSelectionModel().clearSelection();
        cmbDia.getSelectionModel().clearSelection();
        spnInicioH.getValueFactory().setValue(8);
        spnInicioM.getValueFactory().setValue(0);
        spnFinH.getValueFactory().setValue(13);
        spnFinM.getValueFactory().setValue(0);
    }

    @FXML
    public void onGuardar() {
        if (cmbMedico.getValue() == null) { mostrarError("Selecciona un médico"); return; }
        if (cmbDia.getValue() == null)    { mostrarError("Selecciona un día");    return; }
        // Construir horas desde los Spinners (sin riesgo de formato inválido).
        LocalTime hi = LocalTime.of(spnInicioH.getValue(), spnInicioM.getValue());
        LocalTime hf = LocalTime.of(spnFinH.getValue(), spnFinM.getValue());
        if (!hf.isAfter(hi)) { mostrarError("La hora fin debe ser posterior a la hora inicio"); return; }

        Horario h = Horario.builder()
                .medico(cmbMedico.getValue())
                .diaSemana(cmbDia.getValue())
                .horaInicio(hi).horaFin(hf)
                .build();
        try {
            if (seleccionado == null) { service.save(h); mostrarExito("Horario creado"); }
            else { h.setIdHorario(seleccionado.getIdHorario()); service.update(seleccionado.getIdHorario(), h); mostrarExito("Horario actualizado"); }
            cargar(); onLimpiar(); seleccionado = null;
        } catch (Exception ex) { mostrarError("Error: " + ex.getMessage()); }
    }

    @FXML
    public void onModificar() {
        Horario s = tabla.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarError("Selecciona una fila"); return; }
        editar(s);
    }

    @FXML
    public void onEliminar() {
        Horario s = tabla.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarError("Selecciona una fila"); return; }
        confirmarEliminar(s);
    }

    private void editar(Horario h) {
        seleccionado = h;
        if (h.getMedico() != null) {
            cmbMedico.getItems().stream()
                    .filter(m -> m.getIdMedico().equals(h.getMedico().getIdMedico()))
                    .findFirst().ifPresent(cmbMedico::setValue);
        }
        cmbDia.setValue(h.getDiaSemana());
        if (h.getHoraInicio() != null) {
            spnInicioH.getValueFactory().setValue(h.getHoraInicio().getHour());
            spnInicioM.getValueFactory().setValue(h.getHoraInicio().getMinute());
        }
        if (h.getHoraFin() != null) {
            spnFinH.getValueFactory().setValue(h.getHoraFin().getHour());
            spnFinM.getValueFactory().setValue(h.getHoraFin().getMinute());
        }
    }

    private void confirmarEliminar(Horario h) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el horario " + h.getDiaSemana() + " " + h.getHoraInicio() + "-" + h.getHoraFin() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("Confirmar eliminación");
        a.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try { service.delete(h.getIdHorario()); cargar(); mostrarExito("Horario eliminado"); }
            catch (Exception ex) { mostrarError("No se pudo eliminar: " + ex.getMessage()); }
        });
    }

    private Stage stage() { return tabla == null || tabla.getScene() == null ? null : (Stage) tabla.getScene().getWindow(); }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 2800, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2000, s.getX() + 80, s.getY() + 80); }

}
