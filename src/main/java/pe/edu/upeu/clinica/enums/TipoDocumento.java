package pe.edu.upeu.clinica.enums;

// Tipo de documento de identidad. Reusado del repo SysVentas.
// En Clinica2.0 solo se usa DNI en paciente/médico/enfermero, pero queda
// disponible por compatibilidad con futuras integraciones.
public enum TipoDocumento {
    DNI,        // Documento Nacional de Identidad (8 dígitos)
    RUC,        // Registro Único del Contribuyente (11 dígitos)
    CE,         // Carné de Extranjería
    PASAPORTE
}
