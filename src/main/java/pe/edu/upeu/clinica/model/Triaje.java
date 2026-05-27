package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Signos vitales tomados por el enfermero antes de la consulta médica.
// Mapea upeu_triaje. Guardarlo dispara la transición de la cita:
// EN_ESPERA -> TRIAJE -> EN_CONSULTA (ver TriajeServiceImp.guardarTriaje).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Triaje {
    private Long idTriaje;                 // PK
    private Cita cita;                     // FK a la cita que está atendiéndose
    private Enfermero enfermero;           // quién tomó el triaje
    private Double presionSistolica;       // mmHg
    private Double presionDiastolica;      // mmHg
    private Integer frecCardiaca;          // lpm
    private Double temperatura;            // °C
    private Double peso;                   // kg
    private Double talla;                  // metros
    private String motivoConsulta;         // lo que el paciente refiere
    private String observaciones;          // notas del enfermero
    private LocalDateTime fechaReg;        // cuando se tomó el triaje
}
