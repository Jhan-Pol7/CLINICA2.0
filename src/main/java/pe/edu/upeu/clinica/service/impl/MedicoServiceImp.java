package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.repository.MedicoRepository;
import pe.edu.upeu.clinica.service.IMedicoService;

import java.util.List;

// Servicio de Médico — delega CRUD al genérico y expone el filtro
// findByEspecialidad usado por las pantallas de Cita y Reportes.
public class MedicoServiceImp extends CrudGenericoServiceImp<Medico, Long> implements IMedicoService {

    private final MedicoRepository repo;
    public MedicoServiceImp(MedicoRepository repo) { this.repo = repo; }

    @Override
    protected ICrudGenericoRepository<Medico, Long> getRepo() { return repo; }

    @Override
    public List<Medico> findByEspecialidad(Long idEspecialidad) {
        return repo.findByEspecialidad(idEspecialidad);
    }
}
