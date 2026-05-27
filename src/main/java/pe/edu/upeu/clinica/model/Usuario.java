package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuenta de usuario para el login. Mapea upeu_usuario.
// idReferencia apunta a id_medico o id_enfermero según el perfil, para que
// MainConsultaController filtre por médico logueado y MainTriajeController
// registre quién hizo el triaje.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long idUsuario;     // PK auto-generada
    private String usuario;     // login único (no es email)
    private String clave;       // texto plano (en prod se hashearía con BCrypt)
    private String estado;      // ACTIVO | INACTIVO
    private Perfil idPerfil;    // FK al perfil, cargado eager vía JOIN en el repo
    private Long idReferencia;  // id_medico o id_enfermero; null para Root/Admin/Recep
}
