package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Enfermero;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

// CRUD simple sin FKs salientes (los triajes apuntan al enfermero, no al revés).
public class EnfermeroRepository extends AbstractJpaRepository<Enfermero, Long> {

    @Override protected String getTableName() { return "upeu_enfermero"; }
    @Override protected String getPkColumn()  { return "id_enfermero";   }

    @Override
    protected Enfermero insert(Connection connection, Enfermero e) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_enfermero(dni, nombres, apellidos, telefono) VALUES(?,?,?,?)",
                e.getDni(), e.getNombres(), e.getApellidos(), e.getTelefono());
        e.setIdEnfermero(id);
        return e;
    }

    @Override
    protected Enfermero updateRow(Connection connection, Enfermero e) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_enfermero SET dni=?, nombres=?, apellidos=?, telefono=? WHERE id_enfermero=?",
                e.getDni(), e.getNombres(), e.getApellidos(), e.getTelefono(), e.getIdEnfermero());
        return e;
    }

    @Override
    protected Enfermero mapRow(ResultSet rs) throws SQLException {
        return Enfermero.builder()
                .idEnfermero(rs.getLong("id_enfermero"))
                .dni(rs.getString("dni"))
                .nombres(rs.getString("nombres"))
                .apellidos(rs.getString("apellidos"))
                .telefono(rs.getString("telefono"))
                .build();
    }
}
