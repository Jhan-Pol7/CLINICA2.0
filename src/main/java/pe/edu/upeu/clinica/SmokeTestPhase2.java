package pe.edu.upeu.clinica;

import pe.edu.upeu.clinica.config.DatabaseConfig;
import pe.edu.upeu.clinica.enums.DiaSemana;
import pe.edu.upeu.clinica.enums.Sexo;
import pe.edu.upeu.clinica.model.*;
import pe.edu.upeu.clinica.repository.*;
import pe.edu.upeu.clinica.service.IEspecialidadService;
import pe.edu.upeu.clinica.service.IMedicoService;
import pe.edu.upeu.clinica.service.IPacienteService;
import pe.edu.upeu.clinica.service.impl.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Verifica la Fase 2: cada uno de los 6 CRUDs funciona end-to-end contra la BD.
 *
 * Ejecutar:  mvn -q exec:java -Dexec.mainClass="pe.edu.upeu.clinica.SmokeTestPhase2"
 */
public class SmokeTestPhase2 {

    public static void main(String[] args) {
        try {
            DatabaseConfig.init();

            // Instanciación manual (sin AppContext para no requerir JavaFX toolkit)
            EspecialidadRepository espRepo = new EspecialidadRepository();
            MedicoRepository       medRepo = new MedicoRepository();
            HorarioRepository      horRepo = new HorarioRepository();
            EnfermeroRepository    enfRepo = new EnfermeroRepository();
            PacienteRepository     pacRepo = new PacienteRepository();

            IEspecialidadService espSvc = new EspecialidadServiceImp(espRepo);
            IMedicoService       medSvc = new MedicoServiceImp(medRepo);
            IPacienteService     pacSvc = new PacienteServiceImp(pacRepo);

            int ok = 0, total = 0;

            // 1) Especialidad — crear, listar, actualizar, eliminar
            total++;
            int beforeEsp = espSvc.findAll().size();
            Especialidad nueva = espSvc.save(Especialidad.builder()
                    .nombre("Test " + System.currentTimeMillis())
                    .descripcion("desc test").build());
            int afterEsp = espSvc.findAll().size();
            if (afterEsp == beforeEsp + 1 && nueva.getIdEspecialidad() != null) {
                ok++; System.out.println("OK    Especialidad CREATE — id=" + nueva.getIdEspecialidad());
            } else System.err.println("FAIL  Especialidad CREATE");

            total++;
            nueva.setDescripcion("modificada");
            espSvc.update(nueva.getIdEspecialidad(), nueva);
            Especialidad reload = espSvc.findById(nueva.getIdEspecialidad());
            if ("modificada".equals(reload.getDescripcion())) {
                ok++; System.out.println("OK    Especialidad UPDATE");
            } else System.err.println("FAIL  Especialidad UPDATE: " + reload.getDescripcion());

            total++;
            espSvc.delete(nueva.getIdEspecialidad());
            if (espSvc.findAll().size() == beforeEsp) {
                ok++; System.out.println("OK    Especialidad DELETE");
            } else System.err.println("FAIL  Especialidad DELETE");

            // 2) Medico (con FK a Especialidad) — listar con JOIN
            total++;
            List<Medico> medicos = medSvc.findAll();
            boolean medOk = !medicos.isEmpty() && medicos.get(0).getEspecialidad() != null
                    && medicos.get(0).getEspecialidad().getNombre() != null;
            if (medOk) {
                ok++;
                System.out.println("OK    Medico findAll — " + medicos.size() + " registros; "
                        + medicos.get(0).getNombres() + " " + medicos.get(0).getApellidos()
                        + " (esp=" + medicos.get(0).getEspecialidad().getNombre() + ")");
            } else System.err.println("FAIL  Medico findAll");

            // 3) Medico findByEspecialidad
            total++;
            List<Medico> medsCardio = medSvc.findByEspecialidad(3L);
            if (!medsCardio.isEmpty() && medsCardio.stream().allMatch(m -> m.getEspecialidad().getIdEspecialidad().equals(3L))) {
                ok++; System.out.println("OK    Medico findByEspecialidad(3) — " + medsCardio.size() + " médicos");
            } else System.err.println("FAIL  Medico findByEspecialidad(3)");

            // 4) Paciente findByDni
            total++;
            Optional<Paciente> pac = pacSvc.findByDni("43631917");
            if (pac.isPresent() && "Carlos".equals(pac.get().getNombres())) {
                ok++; System.out.println("OK    Paciente findByDni(43631917) — " + pac.get().getNombres() + " " + pac.get().getApellidos());
            } else System.err.println("FAIL  Paciente findByDni(43631917)");

            // 5) Paciente CRUD completo
            total++;
            int beforePac = pacSvc.findAll().size();
            Paciente p = pacSvc.save(Paciente.builder()
                    .dni("99999999").nombres("Test").apellidos("Pruebas")
                    .telefono("999000111").fechaNacimiento(LocalDate.of(2000,1,15))
                    .sexo(Sexo.FEMENINO).direccion("Test 123").email("t@t.pe").build());
            if (pacSvc.findAll().size() == beforePac + 1) {
                ok++; System.out.println("OK    Paciente CREATE — id=" + p.getIdPaciente());
            } else System.err.println("FAIL  Paciente CREATE");
            pacSvc.delete(p.getIdPaciente()); // cleanup

            // 6) Enfermero CRUD
            total++;
            EnfermeroServiceImp enfSvc = new EnfermeroServiceImp(enfRepo);
            int beforeEnf = enfSvc.findAll().size();
            Enfermero e = enfSvc.save(Enfermero.builder()
                    .dni("88888888").nombres("Ana").apellidos("Test").telefono("999111222").build());
            if (enfSvc.findAll().size() == beforeEnf + 1) {
                ok++; System.out.println("OK    Enfermero CREATE — id=" + e.getIdEnfermero());
            } else System.err.println("FAIL  Enfermero CREATE");
            enfSvc.delete(e.getIdEnfermero());

            // 7) Horario CRUD con JOIN a Médico y Especialidad
            total++;
            HorarioServiceImp horSvc = new HorarioServiceImp(horRepo);
            Horario h = horSvc.save(Horario.builder()
                    .medico(medicos.get(0))
                    .diaSemana(DiaSemana.LUN)
                    .horaInicio(LocalTime.of(8, 0))
                    .horaFin(LocalTime.of(13, 0)).build());
            List<Horario> horarios = horSvc.findByMedico(medicos.get(0).getIdMedico());
            if (!horarios.isEmpty() && horarios.get(0).getMedico() != null
                    && horarios.get(0).getMedico().getEspecialidad() != null) {
                ok++;
                System.out.println("OK    Horario CREATE+findByMedico — " + horarios.size() + " horario(s); "
                        + "ej. " + horarios.get(0).getDiaSemana() + " " + horarios.get(0).getHoraInicio() + "-" + horarios.get(0).getHoraFin()
                        + " (med=" + horarios.get(0).getMedico().getNombres()
                        + ", esp=" + horarios.get(0).getMedico().getEspecialidad().getNombre() + ")");
            } else System.err.println("FAIL  Horario CREATE+findByMedico");
            horSvc.delete(h.getIdHorario());

            // 8) Usuario findAll con JOIN a Perfil
            total++;
            UsuarioServiceImp usuSvc = new UsuarioServiceImp(new UsuarioRepository());
            List<Usuario> usuarios = usuSvc.findAll();
            boolean usuOk = !usuarios.isEmpty() && usuarios.get(0).getIdPerfil() != null
                    && usuarios.get(0).getIdPerfil().getNombre() != null;
            if (usuOk) {
                ok++; System.out.println("OK    Usuario findAll — " + usuarios.size() + " usuarios; "
                        + "ej. " + usuarios.get(0).getUsuario() + " (perfil=" + usuarios.get(0).getIdPerfil().getNombre() + ")");
            } else System.err.println("FAIL  Usuario findAll");

            System.out.println();
            System.out.println("=========================");
            System.out.println(" Tests OK: " + ok + " / " + total);
            System.out.println("=========================");
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
