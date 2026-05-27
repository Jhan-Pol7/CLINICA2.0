package pe.edu.upeu.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.clinica.enums.EstadoCita;

import java.time.LocalTime;

// DTO de presentación para listar la cola de pacientes en
// check-in / triaje / consulta (proyección plana de Cita+Paciente+Medico).
// Actualmente los controllers usan directamente Cita; este DTO queda
// disponible si en el futuro se quiere un panel "cola en vivo" más liviano.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColaPacienteDto {
    private Long idCita;
    private String numTicket;
    private String pacienteNombre;
    private String medicoNombre;
    private String especialidadNombre;
    private LocalTime hora;
    private EstadoCita estado;
}
