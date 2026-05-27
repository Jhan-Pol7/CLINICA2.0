package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Enfermero;
import pe.edu.upeu.clinica.model.Triaje;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

// Triaje (signos vitales). Solo se inserta/lee — no se borra ni reescribe;
// si hubiera errores, se registra un nuevo triaje y findByCita devuelve el último.
// El Enfermero es opcional (LEFT JOIN) por si en algún flujo no se identifica.
public class TriajeRepository extends AbstractJpaRepository<Triaje, Long> {

    @Override protected String getTableName() { return "upeu_triaje"; }
    @Override protected String getPkColumn()  { return "id_triaje";   }

    // Devuelve el último triaje de una cita (LIMIT 1 ORDER BY id DESC).
    // Lo consume MainConsultaController para mostrar signos vitales al médico.
    public Optional<Triaje> findByCita(Long idCita) {
        return executeQueryOne(
                "SELECT t.*, e.dni AS e_dni, e.nombres AS e_nombres, e.apellidos AS e_apellidos, e.telefono AS e_tel " +
                "FROM upeu_triaje t LEFT JOIN upeu_enfermero e ON t.id_enfermero = e.id_enfermero " +
                "WHERE t.id_cita = ? ORDER BY t.id_triaje DESC LIMIT 1",
                idCita);
    }

    @Override
    protected Triaje insert(Connection conn, Triaje t) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_triaje(id_cita,id_enfermero,presion_sistolica,presion_diastolica,temperatura,frec_cardiaca,peso,talla,motivo_consulta,observaciones,fecha_reg) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                t.getCita() == null ? null : t.getCita().getIdCita(),
                t.getEnfermero() == null ? null : t.getEnfermero().getIdEnfermero(),
                t.getPresionSistolica(), t.getPresionDiastolica(), t.getTemperatura(),
                t.getFrecCardiaca(), t.getPeso(), t.getTalla(),
                t.getMotivoConsulta(), t.getObservaciones(),
                // Auto-stamp si el caller no proveyó fecha.
                Timestamp.valueOf(t.getFechaReg() == null ? LocalDateTime.now() : t.getFechaReg()));
        t.setIdTriaje(id);
        return t;
    }

    @Override
    protected Triaje updateRow(Connection conn, Triaje t) throws SQLException {
        executeUpdate(conn,
                "UPDATE upeu_triaje SET id_cita=?,id_enfermero=?,presion_sistolica=?,presion_diastolica=?,temperatura=?,frec_cardiaca=?,peso=?,talla=?,motivo_consulta=?,observaciones=? WHERE id_triaje=?",
                t.getCita() == null ? null : t.getCita().getIdCita(),
                t.getEnfermero() == null ? null : t.getEnfermero().getIdEnfermero(),
                t.getPresionSistolica(), t.getPresionDiastolica(), t.getTemperatura(),
                t.getFrecCardiaca(), t.getPeso(), t.getTalla(),
                t.getMotivoConsulta(), t.getObservaciones(),
                t.getIdTriaje());
        return t;
    }

    @Override
    protected Triaje mapRow(ResultSet rs) throws SQLException {
        // Solo guardamos el id de la cita (no se necesita el árbol completo aquí).
        Cita cita = Cita.builder().idCita(rs.getLong("id_cita")).build();

        // Enfermero opcional — puede venir NULL si el triaje fue tomado por root/admin.
        Enfermero enf = null;
        long idEnf = rs.getLong("id_enfermero");
        if (!rs.wasNull()) {
            enf = Enfermero.builder()
                    .idEnfermero(idEnf)
                    .dni(safe(rs, "e_dni"))
                    .nombres(safe(rs, "e_nombres"))
                    .apellidos(safe(rs, "e_apellidos"))
                    .telefono(safe(rs, "e_tel"))
                    .build();
        }
        Timestamp fr = rs.getTimestamp("fecha_reg");
        return Triaje.builder()
                .idTriaje(rs.getLong("id_triaje"))
                .cita(cita)
                .enfermero(enf)
                .presionSistolica(getDouble(rs, "presion_sistolica"))
                .presionDiastolica(getDouble(rs, "presion_diastolica"))
                .temperatura(getDouble(rs, "temperatura"))
                .frecCardiaca(getInt(rs, "frec_cardiaca"))
                .peso(getDouble(rs, "peso"))
                .talla(getDouble(rs, "talla"))
                .motivoConsulta(rs.getString("motivo_consulta"))
                .observaciones(rs.getString("observaciones"))
                .fechaReg(fr == null ? null : fr.toLocalDateTime())
                .build();
    }

    // Tolerante: si la columna no existe en este ResultSet (porque el query no la incluyó),
    // devuelve null en vez de propagar la excepción.
    private String safe(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return null; }
    }
    // wasNull() después de getDouble/getInt para distinguir 0.0 real de NULL en BD.
    private Double getDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col); return rs.wasNull() ? null : v;
    }
    private Integer getInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col); return rs.wasNull() ? null : v;
    }
}
