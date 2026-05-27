package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Receta;
import pe.edu.upeu.clinica.model.RecetaDetalle;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.repository.RecetaDetalleRepository;
import pe.edu.upeu.clinica.repository.RecetaRepository;
import pe.edu.upeu.clinica.service.IRecetaService;

import java.util.List;
import java.util.Optional;

// Servicio de Receta. Compone dos repos: el de Receta para el header
// y el de RecetaDetalle para los medicamentos prescritos.
public class RecetaServiceImp extends CrudGenericoServiceImp<Receta, Long> implements IRecetaService {

    private final RecetaRepository repo;
    private final RecetaDetalleRepository detalleRepo;

    public RecetaServiceImp(RecetaRepository repo, RecetaDetalleRepository detalleRepo) {
        this.repo = repo;
        this.detalleRepo = detalleRepo;
    }

    @Override
    protected ICrudGenericoRepository<Receta, Long> getRepo() { return repo; }

    @Override
    public Optional<Receta> findByConsulta(Long idConsulta) {
        return repo.findByConsulta(idConsulta);
    }

    @Override
    public List<RecetaDetalle> findDetalles(Long idReceta) {
        return detalleRepo.findByReceta(idReceta);
    }
}
