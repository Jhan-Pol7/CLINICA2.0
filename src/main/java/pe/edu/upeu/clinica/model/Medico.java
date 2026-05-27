package pe.edu.upeu.clinica.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo de Médico (profesional de la salud). Se mapea a la tabla upeu_medico.
// Cada médico pertenece a una Especialidad (clave foránea obligatoria) que se
// carga junto a sus datos con un JOIN definido en MedicoRepository.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medico {
    private Long idMedico;              // ID del médico (generado por la BD)

    // El DNI debe tener exactamente 8 dígitos numéricos (formato peruano).
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI solo debe contener 8 dígitos numéricos")
    private String dni;

    @NotBlank(message = "Nombres obligatorios")
    private String nombres;

    @NotBlank(message = "Apellidos obligatorios")
    private String apellidos;

    private String numColegiatura;      // CMP - Colegio Médico del Perú

    // El teléfono debe tener exactamente 9 dígitos (celular peruano).
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos numéricos")
    private String telefono;
    private String email;

    @NotNull(message = "La especialidad es obligatoria")
    private Especialidad especialidad;  // Especialidad del médico (relación con upeu_especialidad)

    // Sirve para que el ComboBox y la tabla muestren el nombre completo del médico
    // sin necesidad de configurar un cellFactory aparte.
    @Override public String toString() {
        return (nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos);
    }
}
