package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Especialidad;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

// Repositorio CRUD simple — Especialidad no tiene FKs hacia otras tablas,
// así que basta con los 6 métodos genéricos + insert/update específicos.
public class EspecialidadRepository extends AbstractJpaRepository<Especialidad, Long> {

    @Override protected String getTableName() { return "upeu_especialidad"; }
    @Override protected String getPkColumn()  { return "id_especialidad";   }

    @Override
    protected Especialidad insert(Connection connection, Especialidad e) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_especialidad(nombre, descripcion) VALUES(?,?)",
                e.getNombre(), e.getDescripcion());
        e.setIdEspecialidad(id);
        return e;
    }

    @Override
    protected Especialidad updateRow(Connection connection, Especialidad e) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_especialidad SET nombre=?, descripcion=? WHERE id_especialidad=?",
                e.getNombre(), e.getDescripcion(), e.getIdEspecialidad());
        return e;
    }

    @Override
    protected Especialidad mapRow(ResultSet rs) throws SQLException {
        return Especialidad.builder()
                .idEspecialidad(rs.getLong("id_especialidad"))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .build();
    }
}
