package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Paciente;

import java.util.Optional;

// Servicio de Paciente — CRUD + búsqueda por DNI (usada al registrar cita
// para precargar datos del paciente y evitar redigitación).
public interface IPacienteService extends ICrudGenericoService<Paciente, Long> {
    Optional<Paciente> findByDni(String dni);
}
