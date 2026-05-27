package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Horario;
import pe.edu.upeu.clinica.repository.HorarioRepository;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.service.IHorarioService;

import java.util.List;

// Servicio de Horario — CRUD + filtrado por médico.
public class HorarioServiceImp extends CrudGenericoServiceImp<Horario, Long> implements IHorarioService {

    private final HorarioRepository repo;
    public HorarioServiceImp(HorarioRepository repo) { this.repo = repo; }

    @Override
    protected ICrudGenericoRepository<Horario, Long> getRepo() { return repo; }

    @Override
    public List<Horario> findByMedico(Long idMedico) { return repo.findByMedico(idMedico); }
}
