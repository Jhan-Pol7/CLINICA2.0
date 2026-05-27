package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Medico;

import java.util.List;

// Servicio de Médico. Aparte del CRUD, expone el filtrado por especialidad
// usado por los combos en cascada de MainCitaController y MainReporteController.
public interface IMedicoService extends ICrudGenericoService<Medico, Long> {
    List<Medico> findByEspecialidad(Long idEspecialidad);
}
