package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import pe.edu.upeu.clinica.components.JasperViewerFX;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.dto.SessionManager;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.model.Especialidad;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.service.IEspecialidadService;
import pe.edu.upeu.clinica.service.IMedicoService;
import pe.edu.upeu.clinica.service.IReporteService;

import java.io.File;
import java.time.LocalDate;

/**
 * Vista de Reportes (JasperReports). Permite filtrar citas por rango de fechas,
 * especialidad, médico y estado, y generar el PDF/visualización.
 */
public class MainReporteController {

    private final IReporteService reporteService;
    private final IEspecialidadService espService;
    private final IMedicoService medService;

    @FXML private DatePicker dpFechaIni, dpFechaFin;
    @FXML private ComboBox<Especialidad> cmbEspecialidad;
    @FXML private ComboBox<Medico> cmbMedico;
    @FXML private ComboBox<EstadoCita> cmbEstado;

    public MainReporteController(IReporteService reporteService,
                                 IEspecialidadService espService,
                                 IMedicoService medService) {
        this.reporteService = reporteService;
        this.espService = espService;
        this.medService = medService;
    }

    @FXML
    // Inicializa los filtros: fechas (último mes a próximo mes),
    // especialidades, médicos (filtrados por especialidad), y estados.
    // Si el usuario logueado es médico, autoselecciona su nombre.
    public void initialize() {
        dpFechaIni.setValue(LocalDate.now().minusMonths(1));
        dpFechaFin.setValue(LocalDate.now().plusMonths(1));

        cmbEspecialidad.setItems(FXCollections.observableArrayList(espService.findAll()));
        cmbEspecialidad.getItems().add(0, null);

        // El médico se filtra por especialidad seleccionada (o todos si especialidad=null)
        refrescarMedicos();
        cmbEspecialidad.valueProperty().addListener((o, a, b) -> refrescarMedicos());

        cmbEstado.setItems(FXCollections.observableArrayList(EstadoCita.values()));
        cmbEstado.getItems().add(0, null);

        // Médico autoselect si el perfil logueado es Medico
        SessionManager s = SessionManager.getInstance();
        if ("Medico".equals(s.getUserPerfil()) && s.getIdReferencia() != null) {
            cmbMedico.getItems().stream()
                    .filter(m -> m != null && m.getIdMedico().equals(s.getIdReferencia()))
                    .findFirst().ifPresent(cmbMedico::setValue);
        }
    }

    // Recarga el ComboBox de médicos filtrado por la especialidad seleccionada.
    // Si no hay especialidad, muestra todos los médicos.
    private void refrescarMedicos() {
        Especialidad esp = cmbEspecialidad.getValue();
        if (esp == null) {
            cmbMedico.setItems(FXCollections.observableArrayList(medService.findAll()));
        } else {
            cmbMedico.setItems(FXCollections.observableArrayList(
                    medService.findByEspecialidad(esp.getIdEspecialidad())));
        }
        cmbMedico.getItems().add(0, null);
    }

    @FXML
    public void onLimpiar() {
        dpFechaIni.setValue(null); dpFechaFin.setValue(null);
        cmbEspecialidad.setValue(null);
        cmbMedico.setValue(null);
        cmbEstado.setValue(null);
    }

    @FXML
    // Genera el reporte de citas con los filtros actuales y lo muestra en el visor Jasper.
    public void onGenerar() {
        try {
            JasperPrint jp = reporteService.generarReporteCitas(
                    dpFechaIni.getValue(),
                    dpFechaFin.getValue(),
                    cmbEspecialidad.getValue() == null ? null : cmbEspecialidad.getValue().getIdEspecialidad(),
                    cmbMedico.getValue()       == null ? null : cmbMedico.getValue().getIdMedico(),
                    cmbEstado.getValue());
            if (jp.getPages().isEmpty()) {
                mostrarInfo("Sin resultados", "No se encontraron citas con esos filtros");
                return;
            }
            JasperViewerFX viewer = new JasperViewerFX();
            viewer.viewReport("Reporte de citas", jp);
        } catch (Exception ex) {
            mostrarError("Error al generar el reporte: " + ex.getMessage());
        }
    }

    @FXML
    // Exporta el reporte de citas como archivo PDF.
    public void onExportarPdf() {
        try {
            JasperPrint jp = reporteService.generarReporteCitas(
                    dpFechaIni.getValue(),
                    dpFechaFin.getValue(),
                    cmbEspecialidad.getValue() == null ? null : cmbEspecialidad.getValue().getIdEspecialidad(),
                    cmbMedico.getValue()       == null ? null : cmbMedico.getValue().getIdMedico(),
                    cmbEstado.getValue());
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar reporte como PDF");
            fc.setInitialFileName("reporte_citas_" + LocalDate.now() + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showSaveDialog(stage());
            if (f == null) return;
            JasperExportManager.exportReportToPdfFile(jp, f.getAbsolutePath());
            mostrarExito("Guardado: " + f.getName());
        } catch (Exception ex) {
            mostrarError("Error al exportar PDF: " + ex.getMessage());
        }
    }

    private Stage stage() {
        return dpFechaIni == null || dpFechaIni.getScene() == null
                ? null : (Stage) dpFechaIni.getScene().getWindow();
    }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 3000, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2400, s.getX() + 80, s.getY() + 80); }
    private void mostrarInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, m);
        a.setHeaderText(t); a.showAndWait();
    }
}
