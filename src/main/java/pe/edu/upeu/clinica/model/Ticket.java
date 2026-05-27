package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

// DTO de impresión. NO se persiste como tabla; se proyecta a partir de una Cita
// vía TicketServiceImp.buildTicket(). Lo consumen el JRXML ticket_cita y el
// renderText() de fallback en MainTicketController.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private String numTicket;        // "T-yyyyMMdd-####"
    private String pacienteNombre;   // "nombres apellidos"
    private String pacienteDni;
    private String especialidad;
    private String medico;           // "Dr. nombres apellidos"
    private LocalDate fecha;
    private LocalTime hora;
    private Integer turno;           // último número del numTicket
    private String tipoAtencion;     // PROGRAMADA | ORDEN_LLEGADA | EMERGENCIA
    private Emisor emisor;           // datos de la clínica
}
