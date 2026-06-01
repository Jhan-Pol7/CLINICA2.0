package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.dto.MenuMenuItenTO;
import pe.edu.upeu.clinica.service.IMenuMenuItemDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Implementa la matriz de accesos por perfil siguiendo el patrón de SysVentas:
//   1) Construir una lista "maestra" con TODOS los items posibles.
//   2) Filtrar por perfil seleccionando índices específicos de esa lista.
// Las etiquetas se internacionalizan con prop() — usa el valor del archivo
// idiomas-XX.properties; si no existe, cae al literal por defecto.
public class MenuMenuItemDaoImp implements IMenuMenuItemDao {

    @Override
    public List<MenuMenuItenTO> listaAccesos(String perfil, Properties idioma) {
        List<MenuMenuItenTO> lista = new ArrayList<>();

        // Etiquetas i18n con fallback a literal en español.
        String mPrincipal = prop(idioma, "menu.nombre.principal",     "Principal");
        String miSalir    = prop(idioma, "menuitem.nombre.salir",     "Salir");
        String mPersonas  = prop(idioma, "menu.nombre.personas",      "Personas");
        String miPaciente = prop(idioma, "menuitem.nombre.paciente",  "Pacientes");
        String miMedico   = prop(idioma, "menuitem.nombre.medico",    "Médicos");
        String miEnferm   = prop(idioma, "menuitem.nombre.enfermero", "Enfermeros");
        String mCatalogos = prop(idioma, "menu.nombre.catalogos",     "Catálogos");
        String miEspec    = prop(idioma, "menuitem.nombre.especialidad", "Especialidades");
        String miHorario  = prop(idioma, "menuitem.nombre.horario",   "Horarios");
        String mSeguridad = prop(idioma, "menu.nombre.seguridad",     "Seguridad");
        String miUsuario  = prop(idioma, "menuitem.nombre.usuario",   "Usuarios");
        String mAtencion  = prop(idioma, "menu.nombre.atencion",      "Atención");
        String miCita     = prop(idioma, "menuitem.nombre.cita",      "Registrar Cita");
        String miCheckin  = prop(idioma, "menuitem.nombre.checkin",   "Check-in");
        String miTriaje   = prop(idioma, "menuitem.nombre.triaje",    "Triaje");
        String miConsult  = prop(idioma, "menuitem.nombre.consulta",  "Consulta");
        String miTicket   = prop(idioma, "menuitem.nombre.ticket",    "Imprimir Ticket");
        String mReporte   = prop(idioma, "menu.nombre.reporte",       "Reportes");
        String miReporte  = prop(idioma, "menuitem.nombre.reporte",   "Citas");
        String miReceta   = prop(idioma, "menuitem.nombre.receta",    "Ver Receta");

        // Lista maestra (los índices se referencian abajo por número).
        // Formato: (id, fxml, menúPadre, textoMenuItem, tituloTab, tipo S/T)
        lista.add(new MenuMenuItenTO("principal",     "/view/login.fxml",           mPrincipal, miSalir,   "Salir",                  "S"));  // 0
        lista.add(new MenuMenuItenTO("paciente",      "/view/main_paciente.fxml",   mPersonas,  miPaciente,"Gestión Pacientes",      "T"));  // 1
        lista.add(new MenuMenuItenTO("medico",        "/view/main_medico.fxml",     mPersonas,  miMedico,  "Gestión Médicos",        "T"));  // 2
        lista.add(new MenuMenuItenTO("enfermero",     "/view/main_enfermero.fxml",  mPersonas,  miEnferm,  "Gestión Enfermeros",     "T"));  // 3
        lista.add(new MenuMenuItenTO("especialidad",  "/view/main_especialidad.fxml", mCatalogos, miEspec, "Gestión Especialidades", "T"));  // 4
        lista.add(new MenuMenuItenTO("horario",       "/view/main_horario.fxml",    mCatalogos, miHorario, "Gestión Horarios",       "T"));  // 5
        lista.add(new MenuMenuItenTO("usuario",       "/view/main_usuario.fxml",    mSeguridad, miUsuario, "Gestión Usuarios",       "T"));  // 6
        lista.add(new MenuMenuItenTO("cita",          "/view/main_cita.fxml",       mAtencion,  miCita,    "Generar Cita+Ticket",    "T"));  // 7
        lista.add(new MenuMenuItenTO("checkin",       "/view/main_checkin.fxml",    mAtencion,  miCheckin, "Confirmar Llegada",      "T"));  // 8
        lista.add(new MenuMenuItenTO("triaje",        "/view/main_triaje.fxml",     mAtencion,  miTriaje,  "Signos Vitales",         "T"));  // 9
        lista.add(new MenuMenuItenTO("consulta",      "/view/main_consulta.fxml",   mAtencion,  miConsult, "Diagnóstico+Receta",     "T"));  // 10
        lista.add(new MenuMenuItenTO("receta",        "/view/main_receta.fxml",     mAtencion,  miReceta,  "Ver Receta",             "T"));  // 11
        lista.add(new MenuMenuItenTO("ticket",        "/view/main_ticket.fxml",     mAtencion,  miTicket,  "Ver/Imprimir Ticket",    "T"));  // 12
        lista.add(new MenuMenuItenTO("reporte",       "/view/main_reporte.fxml",    mReporte,   miReporte, "Reporte de Citas",       "T"));  // 13

        // Selección de items según el perfil. "Salir" (índice 0) siempre va al final.
        List<MenuMenuItenTO> acceso = new ArrayList<>();
        switch (perfil) {
            case "Root":
                // Acceso total — todos los items en orden lógico.
                acceso.addAll(Arrays.asList(
                        lista.get(1), lista.get(2), lista.get(3),                                          // Personas
                        lista.get(4), lista.get(5),                                                        // Catálogos
                        lista.get(6),                                                                      // Seguridad
                        lista.get(7), lista.get(8), lista.get(9), lista.get(10), lista.get(11), lista.get(12), // Atención
                        lista.get(13),                                                                     // Reportes
                        lista.get(0)));                                                                    // Salir
                break;
            case "Administrador":
                // Igual que Root excepto el flujo clínico día-a-día.
                acceso.addAll(Arrays.asList(
                        lista.get(1), lista.get(2), lista.get(3),  // Personas
                        lista.get(4), lista.get(5),                // Catálogos
                        lista.get(6),                              // Seguridad
                        lista.get(13),                             // Reportes
                        lista.get(0)));                            // Salir
                break;
            case "Recepcionista":
                // Solo lo que necesita para atender en mesón.
                acceso.addAll(Arrays.asList(
                        lista.get(1),                              // Pacientes
                        lista.get(7), lista.get(8), lista.get(12), // Cita, Check-in, Ticket
                        lista.get(0)));
                break;
            case "Medico":
                // Solo su consulta + receta + reportes propios.
                acceso.addAll(Arrays.asList(
                        lista.get(10),                             // Consulta
                        lista.get(11),                             // Receta
                        lista.get(13),                             // Reportes
                        lista.get(0)));
                break;
            case "Enfermero":
                // Solo triaje.
                acceso.addAll(Arrays.asList(
                        lista.get(9),                              // Triaje
                        lista.get(0)));
                break;
            default:
                // Defensivo: si el perfil de BD no coincide con ninguno conocido,
                // fallamos rápido en vez de mostrar una UI vacía y silenciosa.
                throw new IllegalArgumentException("Perfil no reconocido: " + perfil);
        }
        return acceso;
    }

    @Override
    public Map<String, String[]> accesosAutorizados(List<MenuMenuItenTO> accesos) {
        // Indexa por el fx:id que MainGuiController da al MenuItem ("mi" + idNombreObj).
        Map<String, String[]> menuConfig = new HashMap<>();
        for (MenuMenuItenTO menu : accesos) {
            menuConfig.put("mi" + menu.getIdNombreObj(),
                    new String[]{menu.getRutaFile(), menu.getNombreTab(), menu.getTipoTab()});
        }
        return menuConfig;
    }

    // Lectura segura de Properties con fallback al literal.
    private String prop(Properties p, String key, String def) {
        if (p == null) return def;
        String v = p.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
