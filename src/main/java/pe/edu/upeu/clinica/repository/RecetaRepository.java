package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Consulta;
import pe.edu.upeu.clinica.model.Receta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

// Receta médica. La lista de detalles se persiste por separado en
// RecetaDetalleRepository (orquestado por ConsultaServiceImp.guardarConsulta).
public class RecetaRepository extends AbstractJpaRepository<Receta, Long> {

    @Override protected String getTableName() { return "upeu_receta"; }
    @Override protected String getPkColumn()  { return "id_receta";   }

    // Receta más reciente asociada a una consulta. Usado por
    // MainRecetaController.renderizar() y el reporte Jasper.
    public Optional<Receta> findByConsulta(Long idConsulta) {
        return executeQueryOne(
                "SELECT * FROM upeu_receta WHERE id_consulta=? ORDER BY id_receta DESC LIMIT 1",
                idConsulta);
    }

    @Override
    protected Receta insert(Connection conn, Receta r) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_receta(id_consulta, indicaciones_generales, recomendaciones, fecha_reg) VALUES(?,?,?,?)",
                r.getConsulta() == null ? null : r.getConsulta().getIdConsulta(),
                r.getIndicacionesGenerales(), r.getRecomendaciones(),
                Timestamp.valueOf(r.getFechaReg() == null ? LocalDateTime.now() : r.getFechaReg()));
        r.setIdReceta(id);
        return r;
    }

    @Override
    protected Receta updateRow(Connection conn, Receta r) throws SQLException {
        executeUpdate(conn,
                "UPDATE upeu_receta SET id_consulta=?, indicaciones_generales=?, recomendaciones=? WHERE id_receta=?",
                r.getConsulta() == null ? null : r.getConsulta().getIdConsulta(),
                r.getIndicacionesGenerales(), r.getRecomendaciones(),
                r.getIdReceta());
        return r;
    }

    @Override
    protected Receta mapRow(ResultSet rs) throws SQLException {
        Timestamp fr = rs.getTimestamp("fecha_reg");
        // Solo idConsulta; la lista de detalles NO se carga aquí.
        Consulta c = Consulta.builder().idConsulta(rs.getLong("id_consulta")).build();
        return Receta.builder()
                .idReceta(rs.getLong("id_receta"))
                .consulta(c)
                .indicacionesGenerales(rs.getString("indicaciones_generales"))
                .recomendaciones(rs.getString("recomendaciones"))
                .fechaReg(fr == null ? null : fr.toLocalDateTime())
                .build();
    }
}
