package pe.edu.upeu.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO ligero que devuelve ConsultaDNI tras scrapear eldni.com.
// Se usa solo para autocompletar el formulario de registro de paciente.
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PersonaDto {
    private String dni;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
}
