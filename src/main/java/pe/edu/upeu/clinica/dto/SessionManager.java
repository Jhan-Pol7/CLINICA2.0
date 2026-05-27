package pe.edu.upeu.clinica.dto;

import lombok.Data;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Consulta;

// Singleton de sesión. Guarda quién está logueado y "handoffs" entre vistas
// (la última cita registrada para mostrar el ticket, la última consulta
// cerrada para mostrar la receta) sin acoplar controllers entre sí.
@Data
public class SessionManager {

    private static SessionManager instance;

    // --- Datos del usuario logueado (poblados por LoginController.abrirMain) ---
    private Long userId;
    private String userName;
    private String userPerfil;     // "Root", "Administrador", "Recepcionista", "Medico", "Enfermero"
    private Long idReferencia;     // id_medico o id_enfermero según perfil; null en otros casos

    // --- Handoffs entre vistas (no se persisten, solo viven en memoria) ---
    private Cita lastCita;         // última cita registrada -> la consume MainTicketController
    private Consulta lastConsulta; // última consulta cerrada -> la consume MainRecetaController

    // Singleton thread-safe (lazy + synchronized).
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
}
