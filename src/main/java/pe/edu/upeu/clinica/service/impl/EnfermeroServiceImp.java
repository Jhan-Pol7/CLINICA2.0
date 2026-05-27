package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Enfermero;
import pe.edu.upeu.clinica.repository.EnfermeroRepository;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.service.IEnfermeroService;

// CRUD puro de Enfermero — solo delega al repositorio.
public class EnfermeroServiceImp extends CrudGenericoServiceImp<Enfermero, Long>
        implements IEnfermeroService {

    private final EnfermeroRepository repo;
    public EnfermeroServiceImp(EnfermeroRepository repo) { this.repo = repo; }

    @Override
    protected ICrudGenericoRepository<Enfermero, Long> getRepo() { return repo; }
}
