package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Receta;
import pe.edu.upeu.clinica.model.RecetaDetalle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

// Línea de medicamento dentro de una receta (relación 1-N con upeu_receta).
public class RecetaDetalleRepository extends AbstractJpaRepository<RecetaDetalle, Long> {

    @Override protected String getTableName() { return "upeu_receta_detalle"; }
    @Override protected String getPkColumn()  { return "id_receta_detalle";   }

    // Devuelve todos los medicamentos de una receta, en orden de inserción.
    // Lo consume MainRecetaController para llenar la tabla.
    public List<RecetaDetalle> findByReceta(Long idReceta) {
        return executeQuery(
                "SELECT * FROM upeu_receta_detalle WHERE id_receta=? ORDER BY id_receta_detalle ASC",
                idReceta);
    }

    // Borrado masivo por receta padre. Útil si en el futuro se quiere editar
    // una receta entera (borrar todas las líneas + reinsertar).
    public int deleteByReceta(Long idReceta) {
        return executeUpdateStandalone("DELETE FROM upeu_receta_detalle WHERE id_receta=?", idReceta);
    }

    @Override
    protected RecetaDetalle insert(Connection conn, RecetaDetalle d) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_receta_detalle(id_receta, medicamento, dosis, frecuencia, duracion, via) VALUES(?,?,?,?,?,?)",
                d.getReceta() == null ? null : d.getReceta().getIdReceta(),
                d.getMedicamento(), d.getDosis(), d.getFrecuencia(), d.getDuracion(), d.getVia());
        d.setIdRecetaDetalle(id);
        return d;
    }

    @Override
    protected RecetaDetalle updateRow(Connection conn, RecetaDetalle d) throws SQLException {
        executeUpdate(conn,
                "UPDATE upeu_receta_detalle SET id_receta=?, medicamento=?, dosis=?, frecuencia=?, duracion=?, via=? WHERE id_receta_detalle=?",
                d.getReceta() == null ? null : d.getReceta().getIdReceta(),
                d.getMedicamento(), d.getDosis(), d.getFrecuencia(), d.getDuracion(), d.getVia(),
                d.getIdRecetaDetalle());
        return d;
    }

    @Override
    protected RecetaDetalle mapRow(ResultSet rs) throws SQLException {
        // Solo idReceta; el árbol completo se hidrata en el caller si lo necesita.
        Receta r = Receta.builder().idReceta(rs.getLong("id_receta")).build();
        return RecetaDetalle.builder()
                .idRecetaDetalle(rs.getLong("id_receta_detalle"))
                .receta(r)
                .medicamento(rs.getString("medicamento"))
                .dosis(rs.getString("dosis"))
                .frecuencia(rs.getString("frecuencia"))
                .duracion(rs.getString("duracion"))
                .via(rs.getString("via"))
                .build();
    }
}
