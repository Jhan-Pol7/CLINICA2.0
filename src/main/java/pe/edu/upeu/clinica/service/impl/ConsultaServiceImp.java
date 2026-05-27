package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Consulta;
import pe.edu.upeu.clinica.model.Receta;
import pe.edu.upeu.clinica.model.RecetaDetalle;
import pe.edu.upeu.clinica.repository.ConsultaRepository;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.repository.RecetaDetalleRepository;
import pe.edu.upeu.clinica.repository.RecetaRepository;
import pe.edu.upeu.clinica.service.ICitaService;
import pe.edu.upeu.clinica.service.IConsultaService;

import java.time.LocalDateTime;
import java.util.Optional;

// Servicio de Consulta — orquesta el cierre del flujo clínico.
// Recibe inyectados los repos de Consulta/Receta/Detalle y el ICitaService
// para realizar la transición final EN_CONSULTA -> ATENDIDA.
public class ConsultaServiceImp extends CrudGenericoServiceImp<Consulta, Long> implements IConsultaService {

    private final ConsultaRepository repo;
    private final RecetaRepository recetaRepo;
    private final RecetaDetalleRepository detalleRepo;
    private final ICitaService citaService;

    public ConsultaServiceImp(ConsultaRepository repo,
                              RecetaRepository recetaRepo,
                              RecetaDetalleRepository detalleRepo,
                              ICitaService citaService) {
        this.repo = repo;
        this.recetaRepo = recetaRepo;
        this.detalleRepo = detalleRepo;
        this.citaService = citaService;
    }

    @Override
    protected ICrudGenericoRepository<Consulta, Long> getRepo() { return repo; }

    @Override
    public Consulta guardarConsulta(Consulta c, Receta receta) {
        if (c.getCita() == null || c.getCita().getIdCita() == null) {
            throw new IllegalArgumentException("La cita es obligatoria");
        }
        // Paso 1: guardar la consulta médica.
        if (c.getFechaReg() == null) c.setFechaReg(LocalDateTime.now());
        Consulta saved = repo.save(c);

        // Paso 2 (opcional): si hay receta, guardarla y todos sus detalles.
        if (receta != null) {
            receta.setConsulta(saved);  // enlazar FK
            if (receta.getFechaReg() == null) receta.setFechaReg(LocalDateTime.now());
            Receta savedReceta = recetaRepo.save(receta);
            if (receta.getDetalles() != null) {
                for (RecetaDetalle d : receta.getDetalles()) {
                    d.setReceta(savedReceta);  // enlazar FK
                    detalleRepo.save(d);
                }
            }
        }
        // Paso 3: cerrar la cita marcándola como ATENDIDA.
        citaService.marcarAtendida(saved.getCita().getIdCita());
        return saved;
    }

    @Override
    public Optional<Consulta> findByCita(Long idCita) { return repo.findByCita(idCita); }
}
