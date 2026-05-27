package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.exception.ModelNotFoundException;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.service.ICrudGenericoService;

import java.util.List;

// Implementación abstracta del CRUD genérico. Cada servicio concreto
// (UsuarioServiceImp, EspecialidadServiceImp, ...) extiende esta clase y
// solo debe proveer su repositorio a través de getRepo().
public abstract class CrudGenericoServiceImp<T, ID> implements ICrudGenericoService<T, ID> {

    // Las subclases declaran su repo específico.
    protected abstract ICrudGenericoRepository<T, ID> getRepo();

    @Override public T save(T t) { return getRepo().save(t); }

    @Override
    public T update(ID id, T t) {
        // Pre-check: si no existe, lanza excepción amigable en vez de ejecutar un UPDATE inocuo.
        if (!getRepo().existsById(id)) {
            throw new ModelNotFoundException("ID no existe: " + id);
        }
        return getRepo().update(t);
    }

    @Override public List<T> findAll() { return getRepo().findAll(); }

    @Override
    public T findById(ID id) {
        // Convierte Optional<T> del repo en T (o excepción).
        return getRepo().findById(id)
                .orElseThrow(() -> new ModelNotFoundException("ID no existe: " + id));
    }

    @Override
    public void delete(ID id) {
        if (!getRepo().existsById(id)) {
            throw new ModelNotFoundException("ID no existe: " + id);
        }
        getRepo().deleteById(id);
    }
}
