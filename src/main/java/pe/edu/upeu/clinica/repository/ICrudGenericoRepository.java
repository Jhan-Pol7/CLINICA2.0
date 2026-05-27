package pe.edu.upeu.clinica.repository;

import java.util.List;
import java.util.Optional;

// Contrato CRUD genérico que implementa AbstractJpaRepository.
// Cualquier repositorio concreto hereda automáticamente los 6 métodos.
public interface ICrudGenericoRepository<T, ID> {
    T save(T entity);              // INSERT con transacción y devuelve la entidad con PK
    T update(T entity);            // UPDATE con transacción
    Optional<T> findById(ID id);   // SELECT por PK (vacío si no existe)
    List<T> findAll();             // SELECT * (sin paginación, suficiente para escritorio)
    void deleteById(ID id);        // DELETE
    boolean existsById(ID id);     // SELECT 1 ... WHERE pk = ?
}
