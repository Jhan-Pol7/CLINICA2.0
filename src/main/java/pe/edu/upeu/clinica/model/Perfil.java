package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Perfil de seguridad (Root, Administrador, Recepcionista, Medico, Enfermero).
// Mapea la tabla upeu_perfil. El campo "nombre" es el que usa
// MenuMenuItemDaoImp.listaAccesos(...) para decidir qué menús se muestran.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {
    private Long idPerfil;      // PK auto-generada
    private String nombre;      // Root, Administrador, Recepcionista, Medico, Enfermero
    private String codigo;      // Código corto (ROOT, ADM, REC, MED, ENF)
}
