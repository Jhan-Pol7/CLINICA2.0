package pe.edu.upeu.clinica.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo de Enfermero/a. Es quien toma el triaje (signos vitales) al paciente.
// Se mapea a la tabla upeu_enfermero. El Usuario con perfil "Enfermero" se enlaza
// a un Enfermero específico mediante Usuario.idReferencia.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enfermero {
    private Long idEnfermero;       // ID del enfermero (generado por la BD)

    // El DNI debe tener exactamente 8 dígitos numéricos (formato peruano).
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI solo debe contener 8 dígitos numéricos")
    private String dni;

    @NotBlank(message = "Nombres obligatorios")
    private String nombres;

    @NotBlank(message = "Apellidos obligatorios")
    private String apellidos;

    // El teléfono debe tener exactamente 9 dígitos (celular peruano).
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos numéricos")
    private String telefono;

    // Sirve para mostrar el nombre completo en ComboBox / TableView sin cellFactory.
    @Override public String toString() {
        return (nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos);
    }
}
