package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Consulta;
import pe.edu.upeu.clinica.model.Receta;

import java.util.Optional;

// Servicio de Consulta médica. La operación clave es guardarConsulta() que
// persiste consulta + receta + detalles y cierra la cita.
public interface IConsultaService extends ICrudGenericoService<Consulta, Long> {

    // Cierre del flujo clínico:
    //   1) inserta la consulta
    //   2) si receta != null, inserta receta y todos sus RecetaDetalle
    //   3) marca la cita como ATENDIDA
    // Si receta es null, se guarda una consulta sin medicación.
    Consulta guardarConsulta(Consulta consulta, Receta receta);

    // Última consulta de una cita (lo consume MainRecetaController).
    Optional<Consulta> findByCita(Long idCita);
}
