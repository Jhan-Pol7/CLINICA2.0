package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.clinica.enums.DiaSemana;

import java.time.LocalTime;

// Franja horaria de atención de un médico en un día de la semana.
// Mapea upeu_horario. La validación "horaFin > horaInicio" no está en BD;
// se comprueba en MainHorarioController.onGuardar().
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Horario {
    private Long idHorario;       // PK auto-generada
    private Medico medico;        // médico al que pertenece la franja
    private DiaSemana diaSemana;  // LUN..DOM (validado por CHECK en BD)
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
