package pe.edu.upeu.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Par genérico (clave/valor) para poblar ComboBox cuando no se quiere
// usar la entidad de dominio directamente.
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComboBoxOption {
    private String key;    // identificador interno
    private String value;  // texto que ve el usuario

    // El ComboBox usa toString() para pintar el item.
    @Override public String toString() { return value; }
}
