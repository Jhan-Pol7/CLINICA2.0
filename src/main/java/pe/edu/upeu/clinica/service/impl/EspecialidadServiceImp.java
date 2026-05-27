package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Especialidad;
import pe.edu.upeu.clinica.repository.EspecialidadRepository;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.service.IEspecialidadService;

// CRUD puro de Especialidad — solo delega al repositorio.
public class EspecialidadServiceImp extends CrudGenericoServiceImp<Especialidad, Long>
        implements IEspecialidadService {

    private final EspecialidadRepository repo;
    public EspecialidadServiceImp(EspecialidadRepository repo) { this.repo = repo; }

    @Override
    protected ICrudGenericoRepository<Especialidad, Long> getRepo() { return repo; }
}
