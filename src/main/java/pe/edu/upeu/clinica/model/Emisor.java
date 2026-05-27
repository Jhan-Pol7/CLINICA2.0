package pe.edu.upeu.clinica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Datos legales/comerciales de la clínica que se imprimen en la cabecera
// de tickets, recetas y reportes. Una sola fila en la tabla upeu_emisor.
// Consumido por ITicketService y los Jasper-reports.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Emisor {
    private Long idEmisor;            // PK
    private String ruc;               // RUC peruano de 11 dígitos
    private String nombreComercial;   // nombre que aparece en la cabecera del ticket
    private String ubigeo;            // código INEI (departamento+provincia+distrito)
    private String domicilioFiscal;   // dirección fiscal completa
    private String urbanizacion;
    private String departamento;
    private String provincia;
    private String distrito;
}
