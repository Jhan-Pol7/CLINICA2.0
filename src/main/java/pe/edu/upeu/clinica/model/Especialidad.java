package pe.edu.upeu.clinica.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Especialidad médica (Medicina General, Pediatría, Cardiología, ...).
// Mapea upeu_especialidad. El toString() devuelve el nombre, así que un
// ComboBox<Especialidad> muestra directamente la etiqueta sin cellFactory.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Especialidad {
    private Long idEspecialidad;        // PK auto-generada

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    private String nombre;

    private String descripcion;         // texto libre, opcional

    // Permite usar la entidad directamente en ComboBox sin cellFactory.
    @Override public String toString() { return nombre; }
}
