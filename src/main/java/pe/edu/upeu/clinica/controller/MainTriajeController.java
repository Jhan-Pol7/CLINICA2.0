package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.components.ColumnInfo;
import pe.edu.upeu.clinica.components.TableViewHelper;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.dto.SessionManager;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Enfermero;
import pe.edu.upeu.clinica.model.Triaje;
import pe.edu.upeu.clinica.repository.EnfermeroRepository;
import pe.edu.upeu.clinica.service.ICitaService;
import pe.edu.upeu.clinica.service.ITriajeService;

import java.util.LinkedHashMap;

/**
 * Enfermería: cita en EN_ESPERA → registrar signos vitales → EN_CONSULTA.
 * El enfermero logueado se determina desde SessionManager.idReferencia.
 */
public class MainTriajeController {

    private final ITriajeService triajeService;
    private final ICitaService citaService;
    private final EnfermeroRepository enfermeroRepo;

    private final ObservableList<Cita> data = FXCollections.observableArrayList();
    private Cita citaSeleccionada;

    @FXML private TableView<Cita> tabla;
    @FXML private Label lblPaciente;
    @FXML private TextField txtSistolica, txtDiastolica, txtTemperatura, txtFrecCard, txtPeso, txtTalla;
    @FXML private TextArea txtMotivo, txtObservaciones;

    public MainTriajeController(ITriajeService triajeService,
                                ICitaService citaService,
                                EnfermeroRepository enfermeroRepo) {
        this.triajeService = triajeService;
        this.citaService = citaService;
        this.enfermeroRepo = enfermeroRepo;
    }

    @FXML
    public void initialize() {
        TableViewHelper<Cita> h = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> cols = new LinkedHashMap<>();
        cols.put("Ticket",       new ColumnInfo("numTicket",            130.0));
        cols.put("Paciente",     new ColumnInfo("paciente.nombres",     140.0));
        cols.put("DNI",          new ColumnInfo("paciente.dni",         100.0));
        cols.put("Médico",       new ColumnInfo("medico.nombres",       130.0));
        cols.put("Especialidad", new ColumnInfo("especialidad.nombre",  130.0));
        cols.put("Hora",         new ColumnInfo("hora",                  80.0));
        h.addColumnsInOrderWithSize(tabla, cols, this::seleccionar, c -> { /* no se elimina */ });
        recargar();
    }

    @FXML
    public void onRecargar() { recargar(); }

    private void recargar() {
        data.setAll(citaService.findByEstado(EstadoCita.EN_ESPERA));
        tabla.setItems(data);
        if (lblPaciente != null) lblPaciente.setText("Selecciona una cita para triajar →");
        citaSeleccionada = null;
        onLimpiar();
    }

    private void seleccionar(Cita c) {
        citaSeleccionada = c;
        if (lblPaciente != null) {
            lblPaciente.setText("Triando: " + c.getNumTicket() + " — "
                    + c.getPaciente().getNombres() + " " + c.getPaciente().getApellidos()
                    + " (DNI " + c.getPaciente().getDni() + ")");
        }
    }

    @FXML
    // Guarda el triaje (signos vitales) y pasa la cita al estado EN_CONSULTA.
    public void onGuardarTriaje() {
        if (citaSeleccionada == null) { mostrarError("Selecciona una cita en la tabla"); return; }
        // Parsea todos los signos vitales. Si alguno tiene formato inválido, se aborta.
        Double sis, dia, temp, peso, talla; Integer fc;
        try {
            sis  = parseDouble(txtSistolica.getText(),    "presión sistólica");
            dia  = parseDouble(txtDiastolica.getText(),   "presión diastólica");
            temp = parseDouble(txtTemperatura.getText(),  "temperatura");
            fc   = parseInt(txtFrecCard.getText(),        "frecuencia cardíaca");
            peso = parseDouble(txtPeso.getText(),         "peso");
            talla= parseDouble(txtTalla.getText(),        "talla");
        } catch (NumberFormatException ex) {
            mostrarError("Valor numérico inválido: " + ex.getMessage());
            return;
        }

        // Obtiene el enfermero logueado a partir del idReferencia de la sesión.
        Enfermero enf = null;
        Long refId = SessionManager.getInstance().getIdReferencia();
        if (refId != null) {
            enf = enfermeroRepo.findById(refId).orElse(null);
        }

        // Construye el objeto Triaje con todos los datos ingresados.
        Triaje t = Triaje.builder()
                .cita(citaSeleccionada)
                .enfermero(enf)
                .presionSistolica(sis)
                .presionDiastolica(dia)
                .temperatura(temp)
                .frecCardiaca(fc)
                .peso(peso)
                .talla(talla)
                .motivoConsulta(empty(txtMotivo.getText()))
                .observaciones(empty(txtObservaciones.getText()))
                .build();
        try {
            triajeService.guardarTriaje(t);
            mostrarExito("Triaje guardado — cita " + citaSeleccionada.getNumTicket() + " enviada a CONSULTA");
            recargar();
        } catch (Exception ex) { mostrarError(ex.getMessage()); }
    }

    @FXML
    // Limpia todos los campos del formulario de triaje.
    public void onLimpiar() {
        txtSistolica.clear(); txtDiastolica.clear(); txtTemperatura.clear();
        txtFrecCard.clear(); txtPeso.clear(); txtTalla.clear();
        txtMotivo.clear(); txtObservaciones.clear();
    }

    // Convierte un texto a Double aceptando coma o punto como separador decimal.
    // Si el texto está vacío devuelve null; si tiene formato inválido lanza excepción.
    private Double parseDouble(String s, String campo) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (NumberFormatException e) { throw new NumberFormatException(campo); }
    }
    // Convierte un texto a Integer. Devuelve null si está vacío.
    private Integer parseInt(String s, String campo) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { throw new NumberFormatException(campo); }
    }

    private Stage stage() { return tabla == null || tabla.getScene() == null ? null : (Stage) tabla.getScene().getWindow(); }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 3000, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2400, s.getX() + 80, s.getY() + 80); }
    private String empty(String s) { return s == null ? "" : s.trim(); }
}
