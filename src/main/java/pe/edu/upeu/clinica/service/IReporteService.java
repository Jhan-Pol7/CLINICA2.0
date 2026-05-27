package pe.edu.upeu.clinica.service;

import net.sf.jasperreports.engine.JasperPrint;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Consulta;

import java.time.LocalDate;

// Fachada de JasperReports. Compila y llena los tres .jrxml del proyecto
// (ticket_cita, receta_medica, reporte_citas) y devuelve JasperPrint listo
// para mostrar en JasperViewerFX o exportar a PDF.
public interface IReporteService {

    // Renderiza ticket_cita.jrxml para una Cita ya persistida.
    JasperPrint generarTicket(Cita cita);

    // Renderiza receta_medica.jrxml para la consulta dada (busca su receta + detalles).
    JasperPrint generarReceta(Consulta consulta);

    // Renderiza reporte_citas.jrxml con filtros opcionales (cualquier param
    // puede ser null = no filtrar por ese campo). Usa el DataSource HikariCP
    // directamente para que el .jrxml ejecute su queryString SQL.
    JasperPrint generarReporteCitas(LocalDate fechaIni, LocalDate fechaFin,
                                    Long idEspecialidad, Long idMedico, EstadoCita estado);
}
