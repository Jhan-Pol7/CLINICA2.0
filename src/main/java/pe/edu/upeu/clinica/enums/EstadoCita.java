package pe.edu.upeu.clinica.enums;

// Estado en el que se encuentra una cita dentro del flujo clínico.
// Las transiciones válidas las garantiza CitaServiceImp:
//   PROGRAMADA -> EN_ESPERA -> TRIAJE -> EN_CONSULTA -> ATENDIDA
//   *          -> CANCELADA  (salvo desde ATENDIDA / CANCELADA)
// La BD tiene un CHECK CONSTRAINT que valida que solo se almacenen estos 6 valores.
public enum EstadoCita {
    PROGRAMADA,   // cita registrada por recepción, aún no llega el paciente
    EN_ESPERA,    // paciente hizo check-in, espera ser llamado a triaje
    TRIAJE,       // enfermero está tomando signos vitales
    EN_CONSULTA,  // triaje terminado, paciente en consultorio
    ATENDIDA,     // consulta y receta cerradas
    CANCELADA     // anulada antes de atenderse
}
