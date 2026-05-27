package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.enums.TipoAtencion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Entidad raíz del flujo clínico. Mapea upeu_cita.
// Estado-máquina (ver CitaServiceImp):
//   PROGRAMADA -> EN_ESPERA -> TRIAJE -> EN_CONSULTA -> ATENDIDA
//   *          -> CANCELADA (excepto desde ATENDIDA/CANCELADA)
// numTicket sigue el formato "T-yyyyMMdd-####" (4 dígitos correlativos por día).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
    private Long idCita;                // PK
    private String numTicket;           // "T-20260527-0001" generado por CitaServiceImp
    private Paciente paciente;          // FK eager via JOIN
    private Medico medico;              // FK eager via JOIN
    private Especialidad especialidad;  // FK eager via JOIN
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoCita estado;          // PROGRAMADA, EN_ESPERA, TRIAJE, EN_CONSULTA, ATENDIDA, CANCELADA
    private TipoAtencion tipoAtencion;  // PROGRAMADA | ORDEN_LLEGADA | EMERGENCIA
    private String motivo;              // texto libre del paciente / recepción
    private LocalDateTime fechaReg;     // cuando se creó la cita
    private Usuario usuarioReg;         // recepcionista que la registró
}
