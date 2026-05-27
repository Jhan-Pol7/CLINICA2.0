package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.enums.TipoAtencion;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.model.Paciente;
import pe.edu.upeu.clinica.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Servicio raíz del flujo clínico. Aparte del CRUD genérico, expone la
// state-machine que garantiza transiciones válidas entre EstadoCita.
public interface ICitaService extends ICrudGenericoService<Cita, Long> {

    // Crea una cita NUEVA en estado PROGRAMADA con numTicket "T-yyyyMMdd-####"
    // correlativo del día. Lanza IllegalStateException si el médico ya tiene
    // otra cita activa en ese slot fecha+hora.
    Cita registrarCita(Paciente paciente, Medico medico, LocalDate fecha,
                       LocalTime hora, TipoAtencion tipo, String motivo, Usuario usuarioReg);

    // PROGRAMADA -> EN_ESPERA. Llamado desde MainCheckinController al
    // confirmar la llegada del paciente.
    Cita checkIn(Long idCita);

    // Cualquier estado activo -> CANCELADA (rechaza si ya está ATENDIDA o CANCELADA).
    Cita cancelar(Long idCita);

    // Transiciones internas usadas por TriajeService / ConsultaService.
    // Se exponen como métodos separados para que cada servicio valide su propia precondición.
    void marcarEnTriaje(Long idCita);     // EN_ESPERA -> TRIAJE
    void marcarEnConsulta(Long idCita);   // TRIAJE/EN_ESPERA -> EN_CONSULTA
    void marcarAtendida(Long idCita);     // EN_CONSULTA -> ATENDIDA

    // Búsquedas para las tablas de las pantallas del flujo.
    List<Cita> findByFecha(LocalDate fecha);
    List<Cita> findByEstado(EstadoCita estado);
    List<Cita> findByEstadoYFecha(EstadoCita estado, LocalDate fecha);
    List<Cita> findByMedicoYFecha(Long idMedico, LocalDate fecha);
    List<Cita> findByMedicoYEstado(Long idMedico, EstadoCita estado);
}
