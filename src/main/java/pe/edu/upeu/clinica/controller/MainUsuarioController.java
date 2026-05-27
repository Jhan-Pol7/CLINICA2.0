package pe.edu.upeu.clinica.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pe.edu.upeu.clinica.components.ColumnInfo;
import pe.edu.upeu.clinica.components.TableViewHelper;
import pe.edu.upeu.clinica.components.Toast;
import pe.edu.upeu.clinica.model.Perfil;
import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.repository.PerfilRepository;
import pe.edu.upeu.clinica.service.IUsuarioService;

import java.util.LinkedHashMap;

// CRUD de Usuario. Permite crear, editar y eliminar cuentas del sistema.
// Cada usuario tiene un Perfil (Root, Administrador, Recepcionista, Medico, Enfermero)
// y opcionalmente una referencia (idReferencia) al médico o enfermero que le corresponde.
public class MainUsuarioController {

    // Servicio de usuarios + repositorio de perfiles (para poblar el ComboBox).
    private final IUsuarioService service;
    private final PerfilRepository perfilRepo;
    private final ObservableList<Usuario> data = FXCollections.observableArrayList();
    private Usuario seleccionado;

    @FXML private TextField     txtUsuario, txtEstado, txtIdReferencia;
    @FXML private PasswordField txtClave;
    @FXML private ComboBox<Perfil> cmbPerfil;
    @FXML private TableView<Usuario> tabla;

    public MainUsuarioController(IUsuarioService service, PerfilRepository perfilRepo) {
        this.service = service;
        this.perfilRepo = perfilRepo;
    }

    @FXML
    // Carga los perfiles en el ComboBox con cellFactory personalizada
    // (Perfil no tiene toString sobreescrito) y configura la tabla con columnas.
    public void initialize() {
        cmbPerfil.setItems(FXCollections.observableArrayList(perfilRepo.findAll()));
        cmbPerfil.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Perfil p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNombre());
            }
        });
        cmbPerfil.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Perfil p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNombre());
            }
        });

        TableViewHelper<Usuario> h = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> cols = new LinkedHashMap<>();
        cols.put("ID",          new ColumnInfo("idUsuario",        60.0));
        cols.put("Usuario",     new ColumnInfo("usuario",         140.0));
        cols.put("Perfil",      new ColumnInfo("idPerfil.nombre", 130.0));
        cols.put("Estado",      new ColumnInfo("estado",           90.0));
        cols.put("idReferencia",new ColumnInfo("idReferencia",    100.0));
        h.addColumnsInOrderWithSize(tabla, cols, this::editar, this::confirmarEliminar);
        cargar();
    }

    private void cargar() { data.setAll(service.findAll()); tabla.setItems(data); }

    @FXML public void onNuevo()   { seleccionado = null; onLimpiar(); txtEstado.setText("ACTIVO"); }
    @FXML public void onLimpiar() {
        txtUsuario.clear(); txtClave.clear(); txtEstado.clear(); txtIdReferencia.clear();
        cmbPerfil.getSelectionModel().clearSelection();
    }

    @FXML
    // Guarda o actualiza un usuario. Valida campos obligatorios y parsea idReferencia.
    public void onGuardar() {
        String usuario = empty(txtUsuario.getText());
        String clave   = txtClave.getText() == null ? "" : txtClave.getText();
        String estado  = empty(txtEstado.getText());
        Perfil perfil  = cmbPerfil.getValue();
        if (usuario.isEmpty() || clave.isEmpty() || estado.isEmpty() || perfil == null) {
            mostrarError("Usuario, clave, estado y perfil son obligatorios"); return;
        }
        Long ref = null;
        if (!empty(txtIdReferencia.getText()).isEmpty()) {
            try { ref = Long.parseLong(txtIdReferencia.getText().trim()); }
            catch (NumberFormatException ex) { mostrarError("idReferencia debe ser numérico"); return; }
        }

        Usuario u = Usuario.builder()
                .usuario(usuario).clave(clave).estado(estado)
                .idPerfil(perfil).idReferencia(ref).build();
        try {
            if (seleccionado == null) { service.save(u); mostrarExito("Usuario creado"); }
            else { u.setIdUsuario(seleccionado.getIdUsuario()); service.update(seleccionado.getIdUsuario(), u); mostrarExito("Usuario actualizado"); }
            cargar(); onLimpiar(); seleccionado = null;
        } catch (Exception ex) { mostrarError("Error: " + ex.getMessage()); }
    }

    @FXML
    public void onModificar() {
        Usuario s = tabla.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarError("Selecciona una fila"); return; }
        editar(s);
    }

    @FXML
    public void onEliminar() {
        Usuario s = tabla.getSelectionModel().getSelectedItem();
        if (s == null) { mostrarError("Selecciona una fila"); return; }
        confirmarEliminar(s);
    }

    // Carga los datos del usuario seleccionado en el formulario para su edición.
    private void editar(Usuario u) {
        seleccionado = u;
        txtUsuario.setText(u.getUsuario()); txtClave.setText(u.getClave());
        txtEstado.setText(u.getEstado());
        txtIdReferencia.setText(u.getIdReferencia() == null ? "" : String.valueOf(u.getIdReferencia()));
        if (u.getIdPerfil() != null) {
            cmbPerfil.getItems().stream()
                    .filter(p -> p.getIdPerfil().equals(u.getIdPerfil().getIdPerfil()))
                    .findFirst().ifPresent(cmbPerfil::setValue);
        }
    }

    // Muestra diálogo de confirmación y, si se acepta, elimina al usuario.
    private void confirmarEliminar(Usuario u) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el usuario \"" + u.getUsuario() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("Confirmar eliminación");
        a.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try { service.delete(u.getIdUsuario()); cargar(); mostrarExito("Usuario eliminado"); }
            catch (Exception ex) { mostrarError("No se pudo eliminar: " + ex.getMessage()); }
        });
    }

    private Stage stage() { return tabla == null || tabla.getScene() == null ? null : (Stage) tabla.getScene().getWindow(); }
    private void mostrarError(String m) { Stage s = stage(); if (s != null) Toast.showToast(s, m, 2800, s.getX() + 80, s.getY() + 80); }
    private void mostrarExito(String m) { Stage s = stage(); if (s != null) Toast.showSuccess(s, m, 2000, s.getX() + 80, s.getY() + 80); }
    private String empty(String s) { return s == null ? "" : s.trim(); }
}
