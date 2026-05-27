package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Triaje;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.repository.TriajeRepository;
import pe.edu.upeu.clinica.service.ICitaService;
import pe.edu.upeu.clinica.service.ITriajeService;

import java.time.LocalDateTime;
import java.util.Optional;

// Servicio de Triaje. Orquesta el guardado del triaje + las dos transiciones
// de estado de la cita asociada (EN_ESPERA -> TRIAJE -> EN_CONSULTA).
// Si cualquier paso falla, los anteriores ya están commiteados — limitación
// conocida por no usar una sola transacción distribuida (igual que SysVentas).
public class TriajeServiceImp extends CrudGenericoServiceImp<Triaje, Long> implements ITriajeService {

    private final TriajeRepository repo;
    private final ICitaService citaService;  // se inyecta para encadenar transiciones

    public TriajeServiceImp(TriajeRepository repo, ICitaService citaService) {
        this.repo = repo;
        this.citaService = citaService;
    }

    @Override
    protected ICrudGenericoRepository<Triaje, Long> getRepo() { return repo; }

    @Override
    public Triaje guardarTriaje(Triaje t) {
        if (t.getCita() == null || t.getCita().getIdCita() == null) {
            throw new IllegalArgumentException("La cita es obligatoria");
        }
        // Paso 1: avanzar la cita a TRIAJE (valida que esté en EN_ESPERA).
        citaService.marcarEnTriaje(t.getCita().getIdCita());
        // Paso 2: persistir el triaje (auto-stamp de fechaReg si no se proveyó).
        if (t.getFechaReg() == null) t.setFechaReg(LocalDateTime.now());
        Triaje saved = repo.save(t);
        // Paso 3: avanzar la cita a EN_CONSULTA para que aparezca en la pantalla del médico.
        citaService.marcarEnConsulta(t.getCita().getIdCita());
        return saved;
    }

    @Override
    public Optional<Triaje> findByCita(Long idCita) { return repo.findByCita(idCita); }
}
