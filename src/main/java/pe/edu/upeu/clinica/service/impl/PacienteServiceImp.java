package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Paciente;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.repository.PacienteRepository;
import pe.edu.upeu.clinica.service.IPacienteService;

import java.util.Optional;

// Servicio de Paciente — CRUD + búsqueda por DNI.
public class PacienteServiceImp extends CrudGenericoServiceImp<Paciente, Long>
        implements IPacienteService {

    private final PacienteRepository repo;
    public PacienteServiceImp(PacienteRepository repo) { this.repo = repo; }

    @Override
    protected ICrudGenericoRepository<Paciente, Long> getRepo() { return repo; }

    @Override
    public Optional<Paciente> findByDni(String dni) { return repo.findByDni(dni); }
}
