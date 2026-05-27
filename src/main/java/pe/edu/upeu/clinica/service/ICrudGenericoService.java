package pe.edu.upeu.clinica.service;

import java.util.List;

// Contrato CRUD genérico que se expone a la capa de presentación.
// La diferencia con ICrudGenericoRepository: aquí findById lanza una
// excepción si no encuentra (en vez de devolver Optional), y update
// recibe explícitamente el ID además de la entidad.
public interface ICrudGenericoService<T, ID> {
    T save(T t);              // INSERT
    T update(ID id, T t);     // UPDATE (verifica que el ID exista primero)
    List<T> findAll();        // SELECT *
    T findById(ID id);        // SELECT por PK (lanza ModelNotFoundException si no existe)
    void delete(ID id);       // DELETE (verifica que el ID exista primero)
}
