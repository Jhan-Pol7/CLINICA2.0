package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Consulta médica realizada por el doctor. Mapea upeu_consulta.
// Una consulta puede generar (opcionalmente) una Receta. Cuando se cierra
// vía ConsultaServiceImp.guardarConsulta, la cita pasa a ATENDIDA.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {
    private Long idConsulta;             // PK
    private Cita cita;                   // FK a la cita (1 cita -> 1 consulta)
    private String sintomas;             // lo que reporta el paciente
    private String diagnostico;          // diagnóstico del médico (obligatorio en UI)
    private String observaciones;        // notas adicionales
    private String examenesSolicitados;  // lab, imágenes, etc.
    private LocalDateTime fechaReg;      // cuando se cerró la consulta
}
