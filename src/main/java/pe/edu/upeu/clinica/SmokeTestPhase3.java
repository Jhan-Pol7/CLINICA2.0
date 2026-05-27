package pe.edu.upeu.clinica;

import pe.edu.upeu.clinica.config.DatabaseConfig;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.enums.TipoAtencion;
import pe.edu.upeu.clinica.model.*;
import pe.edu.upeu.clinica.repository.*;
import pe.edu.upeu.clinica.service.ICitaService;
import pe.edu.upeu.clinica.service.IConsultaService;
import pe.edu.upeu.clinica.service.ITicketService;
import pe.edu.upeu.clinica.service.ITriajeService;
import pe.edu.upeu.clinica.service.impl.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Verifica el flujo clínico end-to-end:
 *   Registrar cita (con numTicket auto) → Check-in → Triaje → Consulta+Receta → ATENDIDA
 *   + cancelación
 *   + idempotencia del schema
 */
public class SmokeTestPhase3 {

    public static void main(String[] args) {
        try {
            DatabaseConfig.init();

            // Wire-up manual (sin AppContext para evitar toolkit JavaFX)
            UsuarioRepository       usuRepo = new UsuarioRepository();
            PacienteRepository      pacRepo = new PacienteRepository();
            MedicoRepository        medRepo = new MedicoRepository();
            EmisorRepository        emiRepo = new EmisorRepository();
            CitaRepository          ciRepo  = new CitaRepository();
            TriajeRepository        triRepo = new TriajeRepository();
            ConsultaRepository      conRepo = new ConsultaRepository();
            RecetaRepository        recRepo = new RecetaRepository();
            RecetaDetalleRepository detRepo = new RecetaDetalleRepository();
            EnfermeroRepository     enfRepo = new EnfermeroRepository();

            ICitaService     citaSvc  = new CitaServiceImp(ciRepo);
            ITriajeService   triSvc   = new TriajeServiceImp(triRepo, citaSvc);
            IConsultaService consSvc  = new ConsultaServiceImp(conRepo, recRepo, detRepo, citaSvc);
            ITicketService   tickSvc  = new TicketServiceImp(emiRepo);

            Paciente paciente = pacRepo.findById(1L).orElseThrow();
            Medico   medico   = medRepo.findById(1L).orElseThrow();
            Usuario  recep    = usuRepo.findById(2L).orElseThrow();
            Enfermero enf     = enfRepo.findById(1L).orElseThrow();

            int ok = 0, total = 0;

            // 1) Registrar cita NUEVA — hora variable para idempotencia entre runs
            total++;
            LocalDate hoy = LocalDate.now();
            int turnoEsperado = ciRepo.getSiguienteTurno(hoy);
            int min = (int) ((System.currentTimeMillis() / 1000) % 60);
            int sec = (int) ((System.currentTimeMillis() / 17) % 60);
            LocalTime hora1 = LocalTime.of(6, min, sec);
            Cita cita = citaSvc.registrarCita(paciente, medico, hoy, hora1,
                    TipoAtencion.PROGRAMADA, "Dolor abdominal", recep);
            String expectedPrefix = "T-";
            if (cita.getNumTicket() != null && cita.getNumTicket().startsWith(expectedPrefix)
                    && cita.getEstado() == EstadoCita.PROGRAMADA
                    && cita.getNumTicket().endsWith(String.format("%04d", turnoEsperado))) {
                ok++;
                System.out.println("OK    Registrar cita — id=" + cita.getIdCita()
                        + " ticket=" + cita.getNumTicket() + " estado=" + cita.getEstado());
            } else System.err.println("FAIL  Registrar cita");

            // 2) Conflicto de horario rechazado (intenta misma hora1)
            total++;
            try {
                citaSvc.registrarCita(paciente, medico, hoy, hora1,
                        TipoAtencion.PROGRAMADA, "dup", recep);
                System.err.println("FAIL  El conflicto de horario debió rechazarse");
            } catch (IllegalStateException ex) {
                ok++;
                System.out.println("OK    Conflicto de horario rechazado: " + ex.getMessage());
            }

            // 3) Check-in
            total++;
            Cita conCheckin = citaSvc.checkIn(cita.getIdCita());
            if (conCheckin.getEstado() == EstadoCita.EN_ESPERA) {
                ok++; System.out.println("OK    Check-in — estado=" + conCheckin.getEstado());
            } else System.err.println("FAIL  Check-in: " + conCheckin.getEstado());

            // 4) Triaje (EN_ESPERA → TRIAJE → EN_CONSULTA)
            total++;
            Triaje t = Triaje.builder()
                    .cita(cita).enfermero(enf)
                    .presionSistolica(120.0).presionDiastolica(80.0)
                    .temperatura(36.7).frecCardiaca(72)
                    .peso(70.0).talla(1.72)
                    .motivoConsulta("Dolor abdominal recurrente")
                    .observaciones("Sin antecedentes relevantes")
                    .build();
            triSvc.guardarTriaje(t);
            Cita post = citaSvc.findById(cita.getIdCita());
            if (post.getEstado() == EstadoCita.EN_CONSULTA) {
                ok++; System.out.println("OK    Triaje guardado y cita en EN_CONSULTA");
            } else System.err.println("FAIL  Triaje: estado=" + post.getEstado());

            // 5) Recuperar triaje
            total++;
            Optional<Triaje> tFound = triSvc.findByCita(cita.getIdCita());
            if (tFound.isPresent() && tFound.get().getTemperatura() == 36.7) {
                ok++; System.out.println("OK    findByCita devuelve triaje correcto");
            } else System.err.println("FAIL  findByCita");

            // 6) Consulta + Receta + Detalles + ATENDIDA
            total++;
            Consulta c = Consulta.builder()
                    .cita(cita)
                    .sintomas("Dolor abdominal, náuseas")
                    .diagnostico("Gastritis aguda")
                    .observaciones("Indicar dieta blanda")
                    .examenesSolicitados("Hemograma, eco abdominal")
                    .build();
            List<RecetaDetalle> meds = new ArrayList<>();
            meds.add(RecetaDetalle.builder().medicamento("Omeprazol 20mg").dosis("1 cap")
                    .frecuencia("c/12h").duracion("14 días").via("VO").build());
            meds.add(RecetaDetalle.builder().medicamento("Paracetamol 500mg").dosis("1 tab")
                    .frecuencia("c/8h").duracion("3 días").via("VO").build());
            Receta r = Receta.builder()
                    .indicacionesGenerales("Tomar después de las comidas")
                    .recomendaciones("Evitar comidas grasas")
                    .detalles(meds).build();
            consSvc.guardarConsulta(c, r);
            Cita atendida = citaSvc.findById(cita.getIdCita());
            if (atendida.getEstado() == EstadoCita.ATENDIDA) {
                ok++; System.out.println("OK    Consulta+Receta cerradas — cita ATENDIDA");
            } else System.err.println("FAIL  Consulta no cerró el flujo: " + atendida.getEstado());

            // 7) Receta detalle persistido
            total++;
            Optional<Consulta> savedCons = consSvc.findByCita(cita.getIdCita());
            if (savedCons.isPresent()) {
                Optional<Receta> rec = recRepo.findByConsulta(savedCons.get().getIdConsulta());
                if (rec.isPresent()) {
                    List<RecetaDetalle> dets = detRepo.findByReceta(rec.get().getIdReceta());
                    if (dets.size() == 2) {
                        ok++; System.out.println("OK    RecetaDetalle: " + dets.size() + " medicamentos persistidos");
                    } else System.err.println("FAIL  RecetaDetalle: " + dets.size());
                } else System.err.println("FAIL  Receta no encontrada");
            } else System.err.println("FAIL  Consulta no encontrada");

            // 8) No se puede triajar una cita ya ATENDIDA
            total++;
            try {
                citaSvc.marcarEnTriaje(cita.getIdCita());
                System.err.println("FAIL  Debió rechazar triaje sobre cita ATENDIDA");
            } catch (IllegalStateException ex) {
                ok++; System.out.println("OK    Rechazo correcto de re-triaje");
            }

            // 9) Cancelar otra cita PROGRAMADA — usar hora diferente para evitar conflicto
            total++;
            LocalTime hora2 = LocalTime.of(7, (int)((System.currentTimeMillis()/19) % 60),
                                              (int)((System.currentTimeMillis()/23) % 60));
            Cita cita2 = citaSvc.registrarCita(paciente, medico, hoy, hora2,
                    TipoAtencion.ORDEN_LLEGADA, "control", recep);
            Cita cancelada = citaSvc.cancelar(cita2.getIdCita());
            if (cancelada.getEstado() == EstadoCita.CANCELADA) {
                ok++; System.out.println("OK    Cancelar cita — estado=" + cancelada.getEstado());
            } else System.err.println("FAIL  Cancelar: " + cancelada.getEstado());

            // 10) Ticket DTO + render
            total++;
            Ticket ticket = tickSvc.buildTicket(cita);
            String rendered = tickSvc.renderText(ticket);
            if (ticket.getEmisor() != null
                    && rendered.contains(ticket.getNumTicket())
                    && rendered.contains("CLÍNICA MÁS CERCA DE DIOS")) {
                ok++;
                System.out.println("OK    Ticket DTO + render (incluye RUC + numTicket + nombre clínica)");
            } else System.err.println("FAIL  Ticket render");

            System.out.println();
            System.out.println("=========================");
            System.out.println(" Tests Fase 3 OK: " + ok + " / " + total);
            System.out.println("=========================");
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
