package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// Receta médica emitida tras una consulta. Mapea upeu_receta.
// La lista "detalles" NO se persiste con la receta automáticamente;
// ConsultaServiceImp.guardarConsulta itera y guarda cada RecetaDetalle.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receta {
    private Long idReceta;                  // PK
    private Consulta consulta;              // FK a la consulta que la origina
    private String indicacionesGenerales;   // "tomar después de las comidas", etc.
    private String recomendaciones;         // "evitar comidas grasas", etc.
    private LocalDateTime fechaReg;         // cuando se emitió la receta
    private List<RecetaDetalle> detalles;   // medicamentos (en memoria, no en JOIN)
}
