package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Perfil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

// Repositorio sin lógica extra: hereda los 6 métodos CRUD genéricos.
// Lo usa MainUsuarioController para poblar el ComboBox de perfiles.
public class PerfilRepository extends AbstractJpaRepository<Perfil, Long> {

    @Override protected String getTableName() { return "upeu_perfil"; }
    @Override protected String getPkColumn()  { return "id_perfil";   }

    @Override
    protected Perfil insert(Connection connection, Perfil entity) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_perfil(nombre,codigo) VALUES(?,?)",
                entity.getNombre(), entity.getCodigo());
        entity.setIdPerfil(id);  // devolvemos la entidad con el id ya asignado
        return entity;
    }

    @Override
    protected Perfil updateRow(Connection connection, Perfil entity) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_perfil SET nombre=?, codigo=? WHERE id_perfil=?",
                entity.getNombre(), entity.getCodigo(), entity.getIdPerfil());
        return entity;
    }

    // Mapea cada fila del ResultSet -> Perfil (usando el Builder de Lombok).
    @Override
    protected Perfil mapRow(ResultSet rs) throws SQLException {
        return Perfil.builder()
                .idPerfil(rs.getLong("id_perfil"))
                .nombre(rs.getString("nombre"))
                .codigo(rs.getString("codigo"))
                .build();
    }
}
