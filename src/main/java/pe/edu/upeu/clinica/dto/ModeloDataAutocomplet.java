package pe.edu.upeu.clinica.dto;

import lombok.Data;

// DTO genérico para componentes de autocompletado (AutoCompleteTextField).
// El toString() devuelve "idx - nameDysplay" que es lo que se muestra
// en el dropdown del autocomplete.
@Data
public class ModeloDataAutocomplet {
    private String idx;          // id/código interno
    private String nameDysplay;  // texto principal a mostrar
    private String otherData;    // info adicional opcional

    @Override public String toString() { return idx + " - " + nameDysplay; }
}
