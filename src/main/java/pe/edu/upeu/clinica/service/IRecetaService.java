package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Receta;
import pe.edu.upeu.clinica.model.RecetaDetalle;

import java.util.List;
import java.util.Optional;

// Servicio de Receta — CRUD + búsqueda por consulta + listado de detalles.
public interface IRecetaService extends ICrudGenericoService<Receta, Long> {
    // Receta más reciente asociada a una consulta.
    Optional<Receta> findByConsulta(Long idConsulta);

    // Medicamentos prescritos en una receta concreta.
    List<RecetaDetalle> findDetalles(Long idReceta);
}
