package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Horario;

import java.util.List;

// Servicio de Horario médico. Aparte del CRUD, permite listar los
// horarios de un médico específico (útil para construir un calendario).
public interface IHorarioService extends ICrudGenericoService<Horario, Long> {
    List<Horario> findByMedico(Long idMedico);
}
