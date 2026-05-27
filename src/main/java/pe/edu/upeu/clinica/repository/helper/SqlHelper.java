package pe.edu.upeu.clinica.repository.helper;

import pe.edu.upeu.clinica.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Capa base de acceso a datos JDBC puro.
// Encapsula la plomería repetitiva (open/close, PreparedStatement, mapeo)
// para que los repositorios concretos solo se ocupen de SQL + mapRow().
public abstract class SqlHelper<T> {

    // Obtiene una conexión del pool HikariCP.
    protected Connection openConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    // Cada repositorio concreto define cómo convertir una fila del ResultSet
    // en una entidad de dominio (manejo de FKs y JOINs incluido).
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    // SELECT que devuelve N filas. Cierra todo automáticamente con try-with-resources.
    protected List<T> executeQuery(String sql, Object... params) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en query: " + e.getMessage(), e);
        }
    }

    // SELECT que devuelve 0 o 1 fila — vacío si no encuentra nada.
    protected Optional<T> executeQueryOne(String sql, Object... params) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en queryOne: " + e.getMessage(), e);
        }
    }

    // "SELECT 1 ... WHERE pk=?" — no llama a mapRow(), solo confirma existencia.
    protected boolean executeExists(String sql, Object id) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en existsById: " + e.getMessage(), e);
        }
    }

    // INSERT que retorna la PK generada por la BD (RETURN_GENERATED_KEYS).
    // No abre conexión: la recibe ya en transacción desde AbstractJpaRepository.save().
    protected long executeInsertGetKey(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("No se generó clave para: " + sql);
            }
        }
    }

    // UPDATE/DELETE dentro de una transacción ya abierta. No hace commit.
    protected void executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            ps.executeUpdate();
        }
    }

    // UPDATE/DELETE en su propia transacción (apertura + commit). Útil para
    // operaciones simples que no se mezclan con otros INSERT/UPDATE.
    protected int executeUpdateStandalone(String sql, Object... params) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            setParams(ps, params);
            int rows = ps.executeUpdate();
            conn.commit();
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Error en executeUpdate: " + e.getMessage(), e);
        }
    }

    // Scalar: leer un único int (típico de COUNT(*), MAX(id), etc.).
    protected int executeScalarInt(String sql, Object... params) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en scalar: " + e.getMessage(), e);
        }
    }

    // Helper: rellena los "?" del PreparedStatement en orden.
    // JDBC usa índices 1-based, por eso (i + 1).
    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
