package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.enums.DiaSemana;
import pe.edu.upeu.clinica.model.Especialidad;
import pe.edu.upeu.clinica.model.Horario;
import pe.edu.upeu.clinica.model.Medico;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

// Horario con doble JOIN (Horario -> Medico -> Especialidad) para hidratar
// el objeto Medico anidado completo con su especialidad en una sola query.
// LocalTime <-> java.sql.Time y DiaSemana enum <-> String.
public class HorarioRepository extends AbstractJpaRepository<Horario, Long> {

    @Override protected String getTableName() { return "upeu_horario"; }
    @Override protected String getPkColumn()  { return "id_horario";   }

    // Alias m_* para evitar colisión de nombres con columnas de upeu_horario
    // (id_medico aparece en ambas tablas).
    private static final String SELECT_JOIN =
            "SELECT h.*, m.dni AS m_dni, m.nombres AS m_nombres, m.apellidos AS m_apellidos, " +
            "       m.num_colegiatura AS m_coleg, m.telefono AS m_tel, m.email AS m_email, " +
            "       m.id_especialidad AS m_id_esp, e.nombre AS esp_nombre " +
            "FROM upeu_horario h " +
            "JOIN upeu_medico m ON h.id_medico = m.id_medico " +
            "JOIN upeu_especialidad e ON m.id_especialidad = e.id_especialidad ";

    @Override
    public List<Horario> findAll() { return executeQuery(SELECT_JOIN); }

    @Override
    public java.util.Optional<Horario> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE h.id_horario = ?", id);
    }

    // Listar todos los horarios de un médico (para el calendario de turnos).
    public List<Horario> findByMedico(Long idMedico) {
        return executeQuery(SELECT_JOIN + "WHERE h.id_medico = ?", idMedico);
    }

    @Override
    protected Horario insert(Connection connection, Horario h) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_horario(id_medico, dia_semana, hora_inicio, hora_fin) VALUES(?,?,?,?)",
                h.getMedico() == null ? null : h.getMedico().getIdMedico(),
                h.getDiaSemana() == null ? null : h.getDiaSemana().name(),  // enum -> String
                h.getHoraInicio() == null ? null : Time.valueOf(h.getHoraInicio()),  // LocalTime -> Time
                h.getHoraFin() == null ? null : Time.valueOf(h.getHoraFin()));
        h.setIdHorario(id);
        return h;
    }

    @Override
    protected Horario updateRow(Connection connection, Horario h) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_horario SET id_medico=?, dia_semana=?, hora_inicio=?, hora_fin=? WHERE id_horario=?",
                h.getMedico() == null ? null : h.getMedico().getIdMedico(),
                h.getDiaSemana() == null ? null : h.getDiaSemana().name(),
                h.getHoraInicio() == null ? null : Time.valueOf(h.getHoraInicio()),
                h.getHoraFin() == null ? null : Time.valueOf(h.getHoraFin()),
                h.getIdHorario());
        return h;
    }

    // Reconstruye el árbol Horario -> Medico -> Especialidad a partir del JOIN.
    @Override
    protected Horario mapRow(ResultSet rs) throws SQLException {
        Especialidad esp = Especialidad.builder()
                .idEspecialidad(rs.getLong("m_id_esp"))
                .nombre(rs.getString("esp_nombre"))
                .build();
        Medico medico = Medico.builder()
                .idMedico(rs.getLong("id_medico"))
                .dni(rs.getString("m_dni"))
                .nombres(rs.getString("m_nombres"))
                .apellidos(rs.getString("m_apellidos"))
                .numColegiatura(rs.getString("m_coleg"))
                .telefono(rs.getString("m_tel"))
                .email(rs.getString("m_email"))
                .especialidad(esp)
                .build();
        Time hi = rs.getTime("hora_inicio");
        Time hf = rs.getTime("hora_fin");
        String dia = rs.getString("dia_semana");
        return Horario.builder()
                .idHorario(rs.getLong("id_horario"))
                .medico(medico)
                .diaSemana(dia == null ? null : DiaSemana.valueOf(dia))    // String -> enum
                .horaInicio(hi == null ? null : hi.toLocalTime())          // Time -> LocalTime
                .horaFin(hf == null ? null : hf.toLocalTime())
                .build();
    }
}
