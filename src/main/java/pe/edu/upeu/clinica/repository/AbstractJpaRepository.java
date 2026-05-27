package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.repository.helper.SqlHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Repositorio CRUD genérico — hereda de SqlHelper para acceso JDBC e
// implementa el contrato ICrudGenericoRepository.
// Cada subclase concreta debe declarar:
//   - getTableName(): nombre físico de la tabla
//   - getPkColumn():  columna de la clave primaria
//   - insert(...):    INSERT a medida (devuelve la PK generada)
//   - updateRow(...): UPDATE a medida
//   - mapRow(rs):     hereda de SqlHelper, mapea ResultSet -> entidad
// save() y update() envuelven la operación en una transacción explícita
// (autoCommit=false + commit/rollback) para que un error a mitad de un
// INSERT con FKs deje la BD limpia.
public abstract class AbstractJpaRepository<T, ID>
        extends SqlHelper<T> implements ICrudGenericoRepository<T, ID> {

    // --- Métodos que define cada subclase concreta ---
    protected abstract String getTableName();
    protected abstract String getPkColumn();
    protected abstract T insert(Connection connection, T entity) throws SQLException;
    protected abstract T updateRow(Connection connection, T entity) throws SQLException;

    @Override
    public T save(T entity) {
        // Transacción manual: si el insert falla, rollback automático.
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = insert(conn, entity);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en guardar: " + e.getMessage(), e);
        }
    }

    @Override
    public T update(T entity) {
        // Mismo patrón transaccional que save().
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = updateRow(conn, entity);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        // SELECT * básico — los repos que necesitan JOINs (Usuario, Medico,
        // Horario, Cita) sobrescriben este método para usar su SELECT_JOIN.
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getPkColumn() + " = ?";
        return executeQueryOne(sql, id);
    }

    @Override
    public List<T> findAll() {
        return executeQuery("SELECT * FROM " + getTableName());
    }

    @Override
    public void deleteById(ID id) {
        executeUpdateStandalone("DELETE FROM " + getTableName() + " WHERE " + getPkColumn() + " = ?", id);
    }

    @Override
    public boolean existsById(ID id) {
        // Usa executeExists() en vez de findById() para no requerir mapRow().
        return executeExists("SELECT 1 FROM " + getTableName() + " WHERE " + getPkColumn() + " = ?", id);
    }
}
