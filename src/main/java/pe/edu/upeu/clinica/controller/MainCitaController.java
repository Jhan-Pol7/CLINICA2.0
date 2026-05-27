package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.components.ColumnInfo;
import pe.edu.upeu.clinica.components.TableViewHelper;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.dto.SessionManager;
import pe.edu.upeu.clinica.enums.TipoAtencion;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Especialidad;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.model.Paciente;
import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.repository.UsuarioRepository;
import pe.edu.upeu.clinica.service.ICitaService;
import pe.edu.upeu.clinica.service.IEspecialidadService;
import pe.edu.upeu.clinica.service.IMedicoService;
import pe.edu.upeu.clinica.service.IPacienteService;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.LinkedHashMap;

/**
 * Vista estrella de Recepción: registrar cita + emisión de ticket.
 *  - Buscar paciente por DNI (auto-rellena nombres/teléfono).
 *  - Combo Especialidad → Combo Médico (filtrado por especialidad).
 *  - Fecha + Hora libres (texto HH:mm).
 *  - Tipo de atención (PROGRAMADA / ORDEN_LLEGADA / EMERGENCIA).
 *  - Tabla con citas del día y su estado.
 */
public class MainCitaController {

    private final ICitaService         citaService;
    private final IPacienteService     pacienteService;
    private final IMedicoService       medicoService;
    private final IEspecialidadService especialidadService;
    private final UsuarioRepository    usuarioRepo;

    private final ObservableList<Cita> data = FXCollections.observableArrayList();
    private Paciente pacienteSeleccionado;

    @FXML private TextField  txtDni, txtNombres, txtApellidos, txtTelefono;
    @FXML private Spinner<Integer> spnHoraH, spnHoraM;  // Spinners para hora (0-23) y minutos (0-59)
    @FXML private TextArea   txtMotivo;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Especialidad> cmbEspecialidad;
    @FXML private ComboBox<Medico>       cmbMedico;
    @FXML private ComboBox<TipoAtencion> cmbTipo;
    @FXML private TableView<Cita> tabla;
    @FXML private Label lblStatus;

    public MainCitaController(ICitaService citaService,
                              IPacienteService pacienteService,
                              IMedicoService medicoService,
                              IEspecialidadService especialidadService,
                              UsuarioRepository usuarioRepo) {
        this.citaService = citaService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
        this.especialidadService = especialidadService;
        this.usuarioRepo = usuarioRepo;
    }

    @FXML
    public void initialize() {
        // El DNI solo permite escribir hasta 8 dígitos numéricos.
        txtDni.setTextFormatter(new TextFormatter<>(cambio -> {
            String nuevoTexto = cambio.getControlNewText();
            return nuevoTexto.matches("\\d{0,8}") ? cambio : null;
        }));

        // Configura los Spinners de hora: horas 0-23 (inicia en 8) y minutos 0-59 (paso de 5).
        // wrapAround=true hace que al pasar de 23 vuelva a 0 (más fácil para el usuario).
        spnHoraH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8, 1));
        spnHoraM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        spnHoraH.getValueFactory().setWrapAround(true);
        spnHoraM.getValueFactory().setWrapAround(true);

        // Carga inicial: trae todas las especialidades disponibles desde la BD.
        cmbEspecialidad.setItems(FXCollections.observableArrayList(especialidadService.findAll()));
        cmbTipo.setItems(FXCollections.observableArrayList(TipoAtencion.values()));
        cmbTipo.setValue(TipoAtencion.PROGRAMADA);
        dpFecha.setValue(LocalDate.now());

        // Al abrir el combo de especialidades, recarga la lista desde la BD.
        // Así aparecen las especialidades nuevas que se acaben de agregar.
        cmbEspecialidad.setOnShowing(e -> {
            Especialidad actual = cmbEspecialidad.getValue();
            cmbEspecialidad.setItems(FXCollections.observableArrayList(especialidadService.findAll()));
            if (actual != null) {
                cmbEspecialidad.getItems().stream()
                        .filter(x -> x.getIdEspecialidad().equals(actual.getIdEspecialidad()))
                        .findFirst().ifPresent(cmbEspecialidad::setValue);
            }
        });

        // Cuando se selecciona una especialidad, filtra los médicos por esa especialidad.
        cmbEspecialidad.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                cmbMedico.setItems(FXCollections.observableArrayList(
                        medicoService.findByEspecialidad(newV.getIdEspecialidad())));
            } else {
                cmbMedico.setItems(FXCollections.observableArrayList());
            }
            cmbMedico.getSelectionModel().clearSelection();
        });

        TableViewHelper<Cita> h = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> cols = new LinkedHashMap<>();
        cols.put("Ticket",       new ColumnInfo("numTicket",            130.0));
        cols.put("Paciente",     new ColumnInfo("paciente.nombres",     140.0));
        cols.put("Médico",       new ColumnInfo("medico.nombres",       130.0));
        cols.put("Especialidad", new ColumnInfo("especialidad.nombre",  130.0));
        cols.put("Hora",         new ColumnInfo("hora",                  80.0));
        cols.put("Estado",       new ColumnInfo("estado",               110.0));
        h.addColumnsInOrderWithSize(tabla, cols, c -> mostrarTicket(c, false), c -> mostrarTicket(c, true));
        recargarTabla();
    }

    private void recargarTabla() {
        data.setAll(citaService.findByFecha(dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue()));
        tabla.setItems(data);
    }

    @FXML
    public void onBuscarPaciente() {
        String dni = empty(txtDni.getText());
        if (dni.isEmpty()) { mostrarError("Ingresa un DNI"); return; }
        pacienteService.findByDni(dni).ifPresentOrElse(p -> {
            pacienteSeleccionado = p;
            txtNombres.setText(p.getNombres());
            txtApellidos.setText(p.getApellidos());
            txtTelefono.setText(p.getTelefono() == null ? "" : p.getTelefono());
            mostrarExito("Paciente encontrado: " + p.getNombres() + " " + p.getApellidos());
        }, () -> {
            pacienteSeleccionado = null;
            mostrarError("No existe paciente con DNI " + dni + ". Regístralo en Personas → Pacientes");
        });
    }

    @FXML
    public void onRegistrarCita() {
        if (pacienteSeleccionado == null)            { mostrarError("Busca primero al paciente por DNI"); return; }
        if (cmbEspecialidad.getValue() == null)      { mostrarError("Selecciona una especialidad");      return; }
        if (cmbMedico.getValue() == null)            { mostrarError("Selecciona un médico");             return; }
        if (dpFecha.getValue() == null)              { mostrarError("Selecciona una fecha");             return; }
        // Construir la hora desde los Spinners (sin posibilidad de formato inválido).
        LocalTime hora = LocalTime.of(spnHoraH.getValue(), spnHoraM.getValue());

        Usuario actor = usuarioRepo.findById(SessionManager.getInstance().getUserId()).orElse(null);
        try {
            Cita cita = citaService.registrarCita(
                    pacienteSeleccionado,
                    cmbMedico.getValue(),
                    dpFecha.getValue(),
                    hora,
                    cmbTipo.getValue(),
                    empty(txtMotivo.getText()),
                    actor);
            SessionManager.getInstance().setLastCita(cita);
            mostrarExito("Cita registrada. Ticket: " + cita.getNumTicket()
                    + " — abre 'Imprimir Ticket' para verlo.");
            if (lblStatus != null) lblStatus.setText("Último ticket: " + cita.getNumTicket());
            recargarTabla();
            onLimpiar();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    @FXML
    public void onLimpiar() {
        pacienteSeleccionado = null;
        txtDni.clear(); txtNombres.clear(); txtApellidos.clear(); txtTelefono.clear();
        spnHoraH.getValueFactory().setValue(8);
        spnHoraM.getValueFactory().setValue(0);
        txtMotivo.clear();
        cmbEspecialidad.getSelectionModel().clearSelection();
        cmbMedico.getSelectionModel().clearSelection();
        cmbTipo.setValue(TipoAtencion.PROGRAMADA);
        dpFecha.setValue(LocalDate.now());
    }

    @FXML
    public void onRecargar() { recargarTabla(); }

    private void mostrarTicket(Cita c, boolean cancelar) {
        if (cancelar) {
            try { citaService.cancelar(c.getIdCita()); recargarTabla(); mostrarExito("Cita cancelada"); }
            catch (Exception ex) { mostrarError(ex.getMessage()); }
        } else {
            SessionManager.getInstance().setLastCita(c);
            mostrarExito("Ticket " + c.getNumTicket() + " seleccionado — abre 'Imprimir Ticket'.");
        }
    }

    private Stage stage() { return tabla == null || tabla.getScene() == null ? null : (Stage) tabla.getScene().getWindow(); }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 3000, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2200, s.getX() + 80, s.getY() + 80); }
    private String empty(String s) { return s == null ? "" : s.trim(); }
}
