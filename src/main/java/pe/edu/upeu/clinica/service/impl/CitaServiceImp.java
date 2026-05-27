package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.enums.TipoAtencion;
import pe.edu.upeu.clinica.exception.ModelNotFoundException;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.model.Paciente;
import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.repository.CitaRepository;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.service.ICitaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Implementación de la state-machine de Cita. Cada transición está protegida
// por una guarda que valida el estado actual antes de moverlo:
//   PROGRAMADA --checkIn()--> EN_ESPERA --marcarEnTriaje()--> TRIAJE
//   TRIAJE --marcarEnConsulta()--> EN_CONSULTA --marcarAtendida()--> ATENDIDA
//   *     --cancelar()--> CANCELADA  (excepto desde ATENDIDA/CANCELADA)
// Si una guarda falla se lanza IllegalStateException y la BD queda intacta.
public class CitaServiceImp extends CrudGenericoServiceImp<Cita, Long> implements ICitaService {

    // Patrón para la parte de fecha del numTicket "T-yyyyMMdd-####".
    private static final DateTimeFormatter TICKET_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CitaRepository repo;
    public CitaServiceImp(CitaRepository repo) { this.repo = repo; }

    @Override
    protected ICrudGenericoRepository<Cita, Long> getRepo() { return repo; }

    @Override
    public Cita registrarCita(Paciente paciente, Medico medico, LocalDate fecha,
                              LocalTime hora, TipoAtencion tipo, String motivo, Usuario usuarioReg) {
        // --- Validaciones de entrada ---
        if (paciente == null || paciente.getIdPaciente() == null) throw new IllegalArgumentException("Paciente requerido");
        if (medico == null || medico.getIdMedico() == null)       throw new IllegalArgumentException("Médico requerido");
        if (medico.getEspecialidad() == null)                     throw new IllegalArgumentException("Médico sin especialidad");
        if (fecha == null || hora == null)                        throw new IllegalArgumentException("Fecha y hora son obligatorios");
        if (tipo == null) tipo = TipoAtencion.PROGRAMADA;

        // --- Validar disponibilidad: no permitir dos citas activas del mismo médico al mismo tiempo ---
        boolean ocupado = repo.findByMedicoYFecha(medico.getIdMedico(), fecha).stream()
                .anyMatch(c -> c.getHora().equals(hora) && c.getEstado() != EstadoCita.CANCELADA);
        if (ocupado) throw new IllegalStateException(
                "El médico ya tiene una cita activa en ese horario");

        // --- Generar el numTicket correlativo del día ---
        int turno = repo.getSiguienteTurno(fecha);
        String numTicket = String.format("T-%s-%04d", fecha.format(TICKET_DATE), turno);

        // --- Construir y persistir la cita en estado inicial PROGRAMADA ---
        Cita cita = Cita.builder()
                .numTicket(numTicket)
                .paciente(paciente)
                .medico(medico)
                .especialidad(medico.getEspecialidad())  // FK redundante (también en upeu_medico), facilita queries por especialidad
                .fecha(fecha).hora(hora)
                .estado(EstadoCita.PROGRAMADA)
                .tipoAtencion(tipo)
                .motivo(motivo)
                .fechaReg(LocalDateTime.now())
                .usuarioReg(usuarioReg)
                .build();
        return repo.save(cita);
    }

    @Override
    public Cita checkIn(Long idCita) {
        Cita c = findById(idCita);
        // Guarda: solo se hace check-in desde PROGRAMADA.
        if (c.getEstado() != EstadoCita.PROGRAMADA) {
            throw new IllegalStateException("Solo se hace check-in a citas PROGRAMADAS (estado actual: " + c.getEstado() + ")");
        }
        repo.updateEstado(idCita, EstadoCita.EN_ESPERA);
        c.setEstado(EstadoCita.EN_ESPERA);  // mantiene el objeto en memoria coherente
        return c;
    }

    @Override
    public Cita cancelar(Long idCita) {
        Cita c = findById(idCita);
        // Guarda: no se puede cancelar una cita ya cerrada.
        if (c.getEstado() == EstadoCita.ATENDIDA || c.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("No se puede cancelar una cita ya " + c.getEstado());
        }
        repo.updateEstado(idCita, EstadoCita.CANCELADA);
        c.setEstado(EstadoCita.CANCELADA);
        return c;
    }

    @Override
    public void marcarEnTriaje(Long idCita) {
        Cita c = findById(idCita);
        if (c.getEstado() != EstadoCita.EN_ESPERA) {
            throw new IllegalStateException("Solo se triajan citas EN_ESPERA (estado actual: " + c.getEstado() + ")");
        }
        repo.updateEstado(idCita, EstadoCita.TRIAJE);
    }

    @Override
    public void marcarEnConsulta(Long idCita) {
        Cita c = findById(idCita);
        // Permite EN_ESPERA como fallback (atajo desde checkin directo a consulta si no hay triaje).
        if (c.getEstado() != EstadoCita.TRIAJE && c.getEstado() != EstadoCita.EN_ESPERA) {
            throw new IllegalStateException("Solo pasan a consulta citas en TRIAJE o EN_ESPERA (estado actual: " + c.getEstado() + ")");
        }
        repo.updateEstado(idCita, EstadoCita.EN_CONSULTA);
    }

    @Override
    public void marcarAtendida(Long idCita) {
        Cita c = findById(idCita);
        if (c.getEstado() != EstadoCita.EN_CONSULTA) {
            throw new IllegalStateException("Solo se cierra como ATENDIDA una cita EN_CONSULTA (estado actual: " + c.getEstado() + ")");
        }
        repo.updateEstado(idCita, EstadoCita.ATENDIDA);
    }

    // Override de findById: usa el SELECT_JOIN del repo (hidrata paciente/medico/especialidad)
    // y lanza una excepción específica si no existe en vez de devolver Optional.
    @Override
    public Cita findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ModelNotFoundException("Cita no existe: " + id));
    }

    // Búsquedas que delegan al repo (sin lógica adicional).
    @Override public List<Cita> findByFecha(LocalDate fecha) { return repo.findByFecha(fecha); }
    @Override public List<Cita> findByEstado(EstadoCita estado) { return repo.findByEstado(estado); }
    @Override public List<Cita> findByEstadoYFecha(EstadoCita e, LocalDate f) { return repo.findByEstadoYFecha(e, f); }
    @Override public List<Cita> findByMedicoYFecha(Long idMedico, LocalDate fecha) { return repo.findByMedicoYFecha(idMedico, fecha); }
    @Override public List<Cita> findByMedicoYEstado(Long idMedico, EstadoCita e) { return repo.findByMedicoYEstado(idMedico, e); }
}
