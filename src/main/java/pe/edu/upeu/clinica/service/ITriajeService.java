package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Triaje;

import java.util.Optional;

// Servicio de Triaje. La operación clave es guardarTriaje() que orquesta
// la transición de la cita asociada: EN_ESPERA -> TRIAJE -> EN_CONSULTA.
public interface ITriajeService extends ICrudGenericoService<Triaje, Long> {
    // Persiste el triaje y avanza la cita al siguiente estado del flujo.
    Triaje guardarTriaje(Triaje triaje);

    // Último triaje registrado para una cita (lo consume MainConsultaController
    // para mostrar al médico los signos vitales tomados por el enfermero).
    Optional<Triaje> findByCita(Long idCita);
}
