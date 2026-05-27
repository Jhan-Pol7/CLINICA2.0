package pe.edu.upeu.clinica.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.clinica.enums.Sexo;

import java.time.LocalDate;

// Paciente atendido en la clínica. Mapea upeu_paciente.
// Las anotaciones Jakarta se evalúan en MainPacienteController.onGuardar();
// si fallan se muestra un Toast rojo con todos los mensajes concatenados.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    private Long idPaciente;            // PK auto-generada

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI solo debe contener 8 dígitos numéricos")
    private String dni;                 // DNI peruano (exactamente 8 dígitos)

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos numéricos")
    private String telefono;            // Teléfono celular peruano (exactamente 9 dígitos)
    private LocalDate fechaNacimiento;  // para cálculo de edad en triaje/consulta
    private Sexo sexo;
    private String direccion;
    private String email;
}
