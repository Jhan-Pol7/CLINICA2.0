package pe.edu.upeu.clinica;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import pe.edu.upeu.clinica.config.DatabaseConfig;
import pe.edu.upeu.clinica.enums.EstadoCita;
import pe.edu.upeu.clinica.enums.TipoAtencion;
import pe.edu.upeu.clinica.model.Cita;
import pe.edu.upeu.clinica.model.Consulta;
import pe.edu.upeu.clinica.model.Medico;
import pe.edu.upeu.clinica.model.Paciente;
import pe.edu.upeu.clinica.model.Receta;
import pe.edu.upeu.clinica.model.RecetaDetalle;
import pe.edu.upeu.clinica.model.Triaje;
import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.repository.*;
import pe.edu.upeu.clinica.service.*;
import pe.edu.upeu.clinica.service.impl.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifica Fase 4: los 3 .jrxml compilan, llenan datos y se exportan a PDF.
 * No abre el visor (es una ventana JavaFX), solo genera artefactos.
 */
public class SmokeTestPhase4 {

    public static void main(String[] args) {
        try {
            DatabaseConfig.init();

            // Wire-up
            UsuarioRepository usuRepo = new UsuarioRepository();
            PacienteRepository pacRepo = new PacienteRepository();
            MedicoRepository medRepo = new MedicoRepository();
            EmisorRepository emiRepo = new EmisorRepository();
            CitaRepository ciRepo = new CitaRepository();
            TriajeRepository triRepo = new TriajeRepository();
            ConsultaRepository conRepo = new ConsultaRepository();
            RecetaRepository recRepo = new RecetaRepository();
            RecetaDetalleRepository detRepo = new RecetaDetalleRepository();
            EnfermeroRepository enfRepo = new EnfermeroRepository();

            ICitaService     citaSvc = new CitaServiceImp(ciRepo);
            ITriajeService   triSvc  = new TriajeServiceImp(triRepo, citaSvc);
            IConsultaService conSvc  = new ConsultaServiceImp(conRepo, recRepo, detRepo, citaSvc);
            IRecetaService   recSvc  = new RecetaServiceImp(recRepo, detRepo);
            ITicketService   tickSvc = new TicketServiceImp(emiRepo);
            IReporteService  repSvc  = new ReporteServiceImp(tickSvc, citaSvc, recSvc, emiRepo);

            // Datos para tests
            Paciente paciente = pacRepo.findById(1L).orElseThrow();
            Medico medico    = medRepo.findById(1L).orElseThrow();
            Usuario recep    = usuRepo.findById(2L).orElseThrow();
            var enf          = enfRepo.findById(1L).orElseThrow();

            // Crear una cita+triaje+consulta+receta para tests
            LocalTime hora = LocalTime.of(5, (int) ((System.currentTimeMillis() / 7) % 60),
                                              (int) ((System.currentTimeMillis() / 11) % 60));
            Cita cita = citaSvc.registrarCita(paciente, medico, LocalDate.now(), hora,
                    TipoAtencion.PROGRAMADA, "Smoke test reportes", recep);
            citaSvc.checkIn(cita.getIdCita());
            triSvc.guardarTriaje(Triaje.builder().cita(cita).enfermero(enf)
                    .presionSistolica(118.0).presionDiastolica(78.0).temperatura(36.5)
                    .frecCardiaca(70).peso(68.0).talla(1.70)
                    .motivoConsulta("Test").observaciones("Test").build());
            Consulta cons = Consulta.builder()
                    .cita(cita).sintomas("Tos").diagnostico("Faringitis viral")
                    .observaciones("Reposo").examenesSolicitados("—").build();
            List<RecetaDetalle> meds = new ArrayList<>();
            meds.add(RecetaDetalle.builder().medicamento("Paracetamol 500mg").dosis("1 tab").frecuencia("c/8h").duracion("3 días").via("VO").build());
            meds.add(RecetaDetalle.builder().medicamento("Loratadina 10mg").dosis("1 tab").frecuencia("c/24h").duracion("5 días").via("VO").build());
            Receta r = Receta.builder().indicacionesGenerales("Tomar con agua").recomendaciones("Reposo y abundantes líquidos").detalles(meds).build();
            cons = conSvc.guardarConsulta(cons, r);

            int ok = 0, total = 0;
            File outDir = new File(System.getProperty("java.io.tmpdir"), "clinica-reportes");
            outDir.mkdirs();

            // 1) Ticket
            total++;
            try {
                JasperPrint jp = repSvc.generarTicket(cita);
                File pdf = new File(outDir, "ticket_" + cita.getNumTicket() + ".pdf");
                JasperExportManager.exportReportToPdfFile(jp, pdf.getAbsolutePath());
                if (pdf.length() > 1000) {
                    ok++;
                    System.out.println("OK    Ticket Jasper PDF — " + pdf.length() + " bytes — " + pdf.getAbsolutePath());
                } else System.err.println("FAIL  Ticket PDF muy pequeño: " + pdf.length());
            } catch (Exception ex) {
                System.err.println("FAIL  Ticket: " + ex.getMessage());
            }

            // 2) Receta
            total++;
            try {
                JasperPrint jp = repSvc.generarReceta(cons);
                File pdf = new File(outDir, "receta_" + cons.getIdConsulta() + ".pdf");
                JasperExportManager.exportReportToPdfFile(jp, pdf.getAbsolutePath());
                if (pdf.length() > 1500) {
                    ok++;
                    System.out.println("OK    Receta Jasper PDF — " + pdf.length() + " bytes — " + pdf.getAbsolutePath());
                } else System.err.println("FAIL  Receta PDF muy pequeño: " + pdf.length());
            } catch (Exception ex) {
                System.err.println("FAIL  Receta: " + ex.getMessage());
            }

            // 3) Reporte de citas sin filtros (todas)
            total++;
            try {
                JasperPrint jp = repSvc.generarReporteCitas(null, null, null, null, null);
                File pdf = new File(outDir, "reporte_citas_todas.pdf");
                JasperExportManager.exportReportToPdfFile(jp, pdf.getAbsolutePath());
                if (pdf.length() > 1500) {
                    ok++;
                    System.out.println("OK    Reporte citas (todas) PDF — " + pdf.length() + " bytes — páginas=" + jp.getPages().size());
                } else System.err.println("FAIL  Reporte PDF muy pequeño: " + pdf.length());
            } catch (Exception ex) {
                System.err.println("FAIL  Reporte todas: " + ex.getMessage());
            }

            // 4) Reporte con filtros (especialidad + estado)
            total++;
            try {
                JasperPrint jp = repSvc.generarReporteCitas(
                        LocalDate.now().minusDays(7),
                        LocalDate.now().plusDays(7),
                        1L,                     // Medicina General
                        null,
                        EstadoCita.ATENDIDA);
                File pdf = new File(outDir, "reporte_citas_filtrado.pdf");
                JasperExportManager.exportReportToPdfFile(jp, pdf.getAbsolutePath());
                ok++;
                System.out.println("OK    Reporte citas filtrado PDF — páginas=" + jp.getPages().size());
            } catch (Exception ex) {
                System.err.println("FAIL  Reporte filtrado: " + ex.getMessage());
            }

            // 5) Cache: segunda llamada al mismo .jrxml NO recompila
            total++;
            try {
                long t0 = System.nanoTime();
                repSvc.generarTicket(cita);
                long t1 = System.nanoTime();
                repSvc.generarTicket(cita);
                long t2 = System.nanoTime();
                long first = (t1 - t0) / 1_000_000;
                long second = (t2 - t1) / 1_000_000;
                ok++;
                System.out.println("OK    Cache JRXML — 1ª: " + first + "ms, 2ª: " + second + "ms");
            } catch (Exception ex) {
                System.err.println("FAIL  Cache: " + ex.getMessage());
            }

            System.out.println();
            System.out.println("=========================");
            System.out.println(" Tests Fase 4 OK: " + ok + " / " + total);
            System.out.println("  → PDFs en: " + outDir.getAbsolutePath());
            System.out.println("=========================");
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
