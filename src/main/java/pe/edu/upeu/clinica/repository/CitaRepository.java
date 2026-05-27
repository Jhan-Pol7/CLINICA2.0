package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.enums.TipoAtencion;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Especialidad;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.model.Paciente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

// Repositorio raíz del flujo clínico. Cada lectura hidrata Paciente + Medico +
// Especialidad en una sola consulta (triple JOIN) para minimizar round-trips
// y evitar el problema N+1 en la TableView principal.
// Métodos extra de búsqueda (findByFecha, findByEstado, findByMedicoYFecha, ...)
// los consumen los controllers de Cita, Check-in, Triaje, Consulta y Reportes.
public class CitaRepository extends AbstractJpaRepository<Cita, Long> {

    @Override protected String getTableName() { return "upeu_cita"; }
    @Override protected String getPkColumn()  { return "id_cita";    }

    // Triple JOIN: paciente, médico y especialidad cargados eager.
    // Los alias p_*, m_*, e_* evitan colisión de nombres entre tablas.
    private static final String SELECT_JOIN =
        "SELECT c.*, " +
        "       p.dni AS p_dni, p.nombres AS p_nombres, p.apellidos AS p_apellidos, " +
        "       p.telefono AS p_tel, p.email AS p_email, " +
        "       m.dni AS m_dni, m.nombres AS m_nombres, m.apellidos AS m_apellidos, " +
        "       m.num_colegiatura AS m_coleg, m.telefono AS m_tel, m.email AS m_email, " +
        "       e.nombre AS e_nombre, e.descripcion AS e_desc " +
        "FROM upeu_cita c " +
        "JOIN upeu_paciente p     ON c.id_paciente = p.id_paciente " +
        "JOIN upeu_medico m       ON c.id_medico = m.id_medico " +
        "JOIN upeu_especialidad e ON c.id_especialidad = e.id_especialidad ";

    @Override
    public List<Cita> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY c.fecha DESC, c.hora ASC");
    }

    // Override obligatorio para hidratar el árbol completo (mapRow lo requiere).
    @Override
    public java.util.Optional<Cita> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE c.id_cita = ?", id);
    }

    // ---------- Búsquedas extra que usan los controllers ----------

    public List<Cita> findByFecha(LocalDate fecha) {
        return executeQuery(SELECT_JOIN + "WHERE c.fecha = ? ORDER BY c.hora ASC", Date.valueOf(fecha));
    }

    // Usado por MainTriajeController (estado=EN_ESPERA) y MainConsultaController (estado=EN_CONSULTA).
    public List<Cita> findByEstado(EstadoCita estado) {
        return executeQuery(SELECT_JOIN + "WHERE c.estado = ? ORDER BY c.fecha DESC, c.hora ASC", estado.name());
    }

    // Cola de check-in: citas PROGRAMADAS del día actual.
    public List<Cita> findByEstadoYFecha(EstadoCita estado, LocalDate fecha) {
        return executeQuery(
                SELECT_JOIN + "WHERE c.estado = ? AND c.fecha = ? ORDER BY c.hora ASC",
                estado.name(), Date.valueOf(fecha));
    }

    // Usado por CitaServiceImp.registrarCita para detectar conflictos de horario.
    public List<Cita> findByMedicoYFecha(Long idMedico, LocalDate fecha) {
        return executeQuery(
                SELECT_JOIN + "WHERE c.id_medico = ? AND c.fecha = ? ORDER BY c.hora ASC",
                idMedico, Date.valueOf(fecha));
    }

    // MainConsultaController filtra por médico logueado (SessionManager.idReferencia)
    // + estado EN_CONSULTA para mostrar solo las citas que le tocan ahora.
    public List<Cita> findByMedicoYEstado(Long idMedico, EstadoCita estado) {
        return executeQuery(
                SELECT_JOIN + "WHERE c.id_medico = ? AND c.estado = ? ORDER BY c.fecha DESC, c.hora ASC",
                idMedico, estado.name());
    }

    // Próximo número correlativo del día. Lo usa CitaServiceImp para generar
    // el numTicket "T-yyyyMMdd-####".
    public int getSiguienteTurno(LocalDate fecha) {
        Integer count = executeScalarInt(
                "SELECT COUNT(*) FROM upeu_cita WHERE fecha = ?", Date.valueOf(fecha));
        return (count == null ? 0 : count) + 1;
    }

    // Cambio quirúrgico de estado para transiciones del flujo
    // (PROGRAMADA->EN_ESPERA, etc.). Evita reescribir toda la fila.
    public void updateEstado(Long idCita, EstadoCita nuevoEstado) {
        executeUpdateStandalone("UPDATE upeu_cita SET estado=? WHERE id_cita=?",
                nuevoEstado.name(), idCita);
    }

    @Override
    protected Cita insert(Connection conn, Cita c) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_cita(num_ticket, id_paciente, id_medico, id_especialidad, fecha, hora, estado, tipo_atencion, motivo, fecha_reg, id_usuario_reg) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                c.getNumTicket(),
                // Solo guardamos los IDs de las FKs.
                c.getPaciente() == null ? null : c.getPaciente().getIdPaciente(),
                c.getMedico()   == null ? null : c.getMedico().getIdMedico(),
                c.getEspecialidad() == null ? null : c.getEspecialidad().getIdEspecialidad(),
                Date.valueOf(c.getFecha()),
                Time.valueOf(c.getHora()),
                c.getEstado().name(),
                c.getTipoAtencion().name(),
                c.getMotivo(),
                // Si el caller no envía fechaReg, usamos NOW().
                c.getFechaReg() == null ? Timestamp.valueOf(java.time.LocalDateTime.now())
                                        : Timestamp.valueOf(c.getFechaReg()),
                c.getUsuarioReg() == null ? null : c.getUsuarioReg().getIdUsuario());
        c.setIdCita(id);
        return c;
    }

    @Override
    protected Cita updateRow(Connection conn, Cita c) throws SQLException {
        // Nota: fecha_reg NO se actualiza, es inmutable tras la creación.
        executeUpdate(conn,
                "UPDATE upeu_cita SET num_ticket=?, id_paciente=?, id_medico=?, id_especialidad=?, fecha=?, hora=?, estado=?, tipo_atencion=?, motivo=?, id_usuario_reg=? WHERE id_cita=?",
                c.getNumTicket(),
                c.getPaciente() == null ? null : c.getPaciente().getIdPaciente(),
                c.getMedico()   == null ? null : c.getMedico().getIdMedico(),
                c.getEspecialidad() == null ? null : c.getEspecialidad().getIdEspecialidad(),
                Date.valueOf(c.getFecha()),
                Time.valueOf(c.getHora()),
                c.getEstado().name(),
                c.getTipoAtencion().name(),
                c.getMotivo(),
                c.getUsuarioReg() == null ? null : c.getUsuarioReg().getIdUsuario(),
                c.getIdCita());
        return c;
    }

    // Reconstruye Cita + Paciente + Medico + Especialidad anidados a partir
    // del triple JOIN. El árbol queda listo para usar en la UI.
    @Override
    protected Cita mapRow(ResultSet rs) throws SQLException {
        Paciente p = Paciente.builder()
                .idPaciente(rs.getLong("id_paciente"))
                .dni(rs.getString("p_dni"))
                .nombres(rs.getString("p_nombres"))
                .apellidos(rs.getString("p_apellidos"))
                .telefono(rs.getString("p_tel"))
                .email(rs.getString("p_email"))
                .build();
        Especialidad esp = Especialidad.builder()
                .idEspecialidad(rs.getLong("id_especialidad"))
                .nombre(rs.getString("e_nombre"))
                .descripcion(rs.getString("e_desc"))
                .build();
        Medico m = Medico.builder()
                .idMedico(rs.getLong("id_medico"))
                .dni(rs.getString("m_dni"))
                .nombres(rs.getString("m_nombres"))
                .apellidos(rs.getString("m_apellidos"))
                .numColegiatura(rs.getString("m_coleg"))
                .telefono(rs.getString("m_tel"))
                .email(rs.getString("m_email"))
                .especialidad(esp)  // misma instancia que arriba: Cita.especialidad == Cita.medico.especialidad
                .build();
        Time hora = rs.getTime("hora");
        Date fecha = rs.getDate("fecha");
        Timestamp fr = rs.getTimestamp("fecha_reg");
        return Cita.builder()
                .idCita(rs.getLong("id_cita"))
                .numTicket(rs.getString("num_ticket"))
                .paciente(p)
                .medico(m)
                .especialidad(esp)
                .fecha(fecha == null ? null : fecha.toLocalDate())
                .hora(hora == null ? null : hora.toLocalTime())
                .estado(EstadoCita.valueOf(rs.getString("estado")))
                .tipoAtencion(TipoAtencion.valueOf(rs.getString("tipo_atencion")))
                .motivo(rs.getString("motivo"))
                .fechaReg(fr == null ? null : fr.toLocalDateTime())
                .build();
    }
}
