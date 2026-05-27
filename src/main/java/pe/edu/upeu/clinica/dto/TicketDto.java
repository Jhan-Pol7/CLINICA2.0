package pe.edu.upeu.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.clinica.model.Ticket;

// Wrapper de Ticket para enviar al servicio de impresión.
// Actualmente solo encapsula el modelo Ticket; existe por si en el futuro
// se añaden metadatos de impresión (nº copias, formato, etc.).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    private Ticket ticket;
}
