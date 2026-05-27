package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Especialidad;
import pe.edu.upeu.clinica.model.Medico;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

// Médico con su Especialidad cargada eager vía JOIN.
// Necesitamos JOIN porque mapRow construye un Especialidad anidado con su
// nombre/descripcion — sin el JOIN faltarían las columnas alias.
public class MedicoRepository extends AbstractJpaRepository<Medico, Long> {

    @Override protected String getTableName() { return "upeu_medico"; }
    @Override protected String getPkColumn()  { return "id_medico";   }

    // Los alias esp_nombre / esp_descripcion evitan colisión con campos del médico
    // y son los que lee mapRow().
    private static final String SELECT_JOIN =
            "SELECT m.*, e.nombre AS esp_nombre, e.descripcion AS esp_descripcion " +
            "FROM upeu_medico m JOIN upeu_especialidad e ON m.id_especialidad = e.id_especialidad ";

    @Override
    public List<Medico> findAll() { return executeQuery(SELECT_JOIN); }

    // Override obligatorio: el SELECT * genérico no tiene esp_nombre/esp_descripcion
    // y mapRow() rompería.
    @Override
    public java.util.Optional<Medico> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE m.id_medico = ?", id);
    }

    // Filtra médicos por especialidad — usado por el combo en cascada de MainCita.
    public List<Medico> findByEspecialidad(Long idEspecialidad) {
        return executeQuery(SELECT_JOIN + "WHERE m.id_especialidad = ?", idEspecialidad);
    }

    @Override
    protected Medico insert(Connection connection, Medico m) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_medico(dni, nombres, apellidos, num_colegiatura, telefono, email, id_especialidad) VALUES(?,?,?,?,?,?,?)",
                m.getDni(), m.getNombres(), m.getApellidos(),
                m.getNumColegiatura(), m.getTelefono(), m.getEmail(),
                // Solo guardamos el id_especialidad (FK); el objeto Especialidad
                // no se persiste anidado.
                m.getEspecialidad() == null ? null : m.getEspecialidad().getIdEspecialidad());
        m.setIdMedico(id);
        return m;
    }

    @Override
    protected Medico updateRow(Connection connection, Medico m) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_medico SET dni=?, nombres=?, apellidos=?, num_colegiatura=?, telefono=?, email=?, id_especialidad=? WHERE id_medico=?",
                m.getDni(), m.getNombres(), m.getApellidos(),
                m.getNumColegiatura(), m.getTelefono(), m.getEmail(),
                m.getEspecialidad() == null ? null : m.getEspecialidad().getIdEspecialidad(),
                m.getIdMedico());
        return m;
    }

    // Hidrata Medico + Especialidad anidada a partir del JOIN.
    @Override
    protected Medico mapRow(ResultSet rs) throws SQLException {
        Especialidad esp = Especialidad.builder()
                .idEspecialidad(rs.getLong("id_especialidad"))
                .nombre(rs.getString("esp_nombre"))
                .descripcion(rs.getString("esp_descripcion"))
                .build();
        return Medico.builder()
                .idMedico(rs.getLong("id_medico"))
                .dni(rs.getString("dni"))
                .nombres(rs.getString("nombres"))
                .apellidos(rs.getString("apellidos"))
                .numColegiatura(rs.getString("num_colegiatura"))
                .telefono(rs.getString("telefono"))
                .email(rs.getString("email"))
                .especialidad(esp)
                .build();
    }
}
