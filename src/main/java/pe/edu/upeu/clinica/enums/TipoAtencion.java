package pe.edu.upeu.clinica.enums;

// Modalidad de la cita en cuanto a planificación.
// La BD tiene un CHECK CONSTRAINT con estos 3 valores.
public enum TipoAtencion {
    PROGRAMADA,     // cita reservada con anticipación, hora fija
    ORDEN_LLEGADA,  // sin reserva previa, se atiende por orden de llegada
    EMERGENCIA      // urgencia médica, prioridad máxima
}
