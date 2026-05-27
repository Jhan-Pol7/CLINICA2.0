package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

// Consulta médica. La FK a Cita se almacena como id (Consulta.cita.idCita);
// el árbol completo no se hidrata aquí — el caller ya tiene la Cita en memoria
// cuando navega Triaje -> Consulta.
public class ConsultaRepository extends AbstractJpaRepository<Consulta, Long> {

    @Override protected String getTableName() { return "upeu_consulta"; }
    @Override protected String getPkColumn()  { return "id_consulta";   }

    // Última consulta asociada a una cita (si existieran reaperturas, devuelve la más reciente).
    public Optional<Consulta> findByCita(Long idCita) {
        return executeQueryOne(
                "SELECT * FROM upeu_consulta WHERE id_cita=? ORDER BY id_consulta DESC LIMIT 1",
                idCita);
    }

    @Override
    protected Consulta insert(Connection conn, Consulta c) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_consulta(id_cita,sintomas,diagnostico,observaciones,examenes_solicitados,fecha_reg) VALUES(?,?,?,?,?,?)",
                c.getCita() == null ? null : c.getCita().getIdCita(),
                c.getSintomas(), c.getDiagnostico(), c.getObservaciones(), c.getExamenesSolicitados(),
                Timestamp.valueOf(c.getFechaReg() == null ? LocalDateTime.now() : c.getFechaReg()));
        c.setIdConsulta(id);
        return c;
    }

    @Override
    protected Consulta updateRow(Connection conn, Consulta c) throws SQLException {
        // fecha_reg es inmutable tras la creación.
        executeUpdate(conn,
                "UPDATE upeu_consulta SET id_cita=?,sintomas=?,diagnostico=?,observaciones=?,examenes_solicitados=? WHERE id_consulta=?",
                c.getCita() == null ? null : c.getCita().getIdCita(),
                c.getSintomas(), c.getDiagnostico(), c.getObservaciones(), c.getExamenesSolicitados(),
                c.getIdConsulta());
        return c;
    }

    @Override
    protected Consulta mapRow(ResultSet rs) throws SQLException {
        Timestamp fr = rs.getTimestamp("fecha_reg");
        // Solo idCita; si el caller necesita la cita completa, llama a CitaService.findById.
        Cita cita = Cita.builder().idCita(rs.getLong("id_cita")).build();
        return Consulta.builder()
                .idConsulta(rs.getLong("id_consulta"))
                .cita(cita)
                .sintomas(rs.getString("sintomas"))
                .diagnostico(rs.getString("diagnostico"))
                .observaciones(rs.getString("observaciones"))
                .examenesSolicitados(rs.getString("examenes_solicitados"))
                .fechaReg(fr == null ? null : fr.toLocalDateTime())
                .build();
    }
}
