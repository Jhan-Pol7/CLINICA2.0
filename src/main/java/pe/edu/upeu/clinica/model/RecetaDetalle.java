package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Una línea (un medicamento) dentro de una Receta. Mapea upeu_receta_detalle.
// Una receta tiene N detalles (1 medicamento = 1 fila).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDetalle {
    private Long idRecetaDetalle;  // PK
    private Receta receta;         // FK a la receta padre
    private String medicamento;    // "Paracetamol 500mg"
    private String dosis;          // "1 tab"
    private String frecuencia;     // "c/8h"
    private String duracion;       // "5 días"
    private String via;            // "VO" (vía oral), "IM", etc.
}
