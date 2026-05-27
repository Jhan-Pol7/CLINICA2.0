package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Perfil;
import pe.edu.upeu.clinica.model.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Repositorio de Usuario con JOIN a Perfil — cada vez que se lee un usuario
// se carga eager su perfil (necesario para que el menú dinámico pueda
// determinar el rol del logueado sin un segundo round-trip).
public class UsuarioRepository extends AbstractJpaRepository<Usuario, Long> {

    @Override protected String getTableName() { return "upeu_usuario"; }
    @Override protected String getPkColumn()  { return "id_usuario";   }

    // SQL base con JOIN; las consultas concretas le añaden la cláusula WHERE.
    private static final String SELECT_JOIN =
            "SELECT u.*, p.nombre AS perfil_nombre, p.codigo AS perfil_codigo " +
            "FROM upeu_usuario u JOIN upeu_perfil p ON u.id_perfil = p.id_perfil ";

    @Override
    public List<Usuario> findAll() { return executeQuery(SELECT_JOIN); }

    // Sobrescribimos findById para usar el JOIN (mapRow necesita perfil_nombre/perfil_codigo).
    @Override
    public Optional<Usuario> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE u.id_usuario = ?", id);
    }

    // Búsqueda por nombre de usuario (sin contraseña). Útil para validaciones.
    public Optional<Usuario> buscarUsuario(String usuario) {
        return executeQueryOne(SELECT_JOIN + "WHERE u.usuario = ?", usuario);
    }

    // Login: compara usuario + clave en texto plano (sin hash en esta versión).
    public Optional<Usuario> findByUsuarioAndClave(String usuario, String clave) {
        return executeQueryOne(SELECT_JOIN + "WHERE u.usuario = ? AND u.clave = ?", usuario, clave);
    }

    @Override
    protected Usuario insert(Connection connection, Usuario entity) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_usuario(usuario,clave,estado,id_perfil,id_referencia) VALUES(?,?,?,?,?)",
                entity.getUsuario(),
                entity.getClave(),
                entity.getEstado(),
                entity.getIdPerfil() != null ? entity.getIdPerfil().getIdPerfil() : null,
                entity.getIdReferencia());
        entity.setIdUsuario(id);
        return entity;
    }

    @Override
    protected Usuario updateRow(Connection connection, Usuario entity) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_usuario SET usuario=?,clave=?,estado=?,id_perfil=?,id_referencia=? WHERE id_usuario=?",
                entity.getUsuario(),
                entity.getClave(),
                entity.getEstado(),
                entity.getIdPerfil() != null ? entity.getIdPerfil().getIdPerfil() : null,
                entity.getIdReferencia(),
                entity.getIdUsuario());
        return entity;
    }

    // mapRow construye Usuario + Perfil anidado a partir del JOIN.
    @Override
    protected Usuario mapRow(ResultSet rs) throws SQLException {
        Perfil perfil = Perfil.builder()
                .idPerfil(rs.getLong("id_perfil"))
                .nombre(rs.getString("perfil_nombre"))
                .codigo(rs.getString("perfil_codigo"))
                .build();
        // id_referencia puede ser NULL (admin/recep no apuntan a médico/enfermero).
        Long idRef = rs.getObject("id_referencia") == null ? null : rs.getLong("id_referencia");
        return Usuario.builder()
                .idUsuario(rs.getLong("id_usuario"))
                .usuario(rs.getString("usuario"))
                .clave(rs.getString("clave"))
                .estado(rs.getString("estado"))
                .idPerfil(perfil)
                .idReferencia(idRef)
                .build();
    }
}
