package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.dto.MenuMenuItenTO;

import java.util.List;
import java.util.Map;
import java.util.Properties;

// Servicio que decide qué menús ve cada perfil de usuario. Lo consume
// MainGuiController.graficarMenus() al construir el MenuBar dinámico.
public interface IMenuMenuItemDao {

    // Devuelve la lista de MenuItems autorizados para el perfil dado,
    // con los textos ya traducidos según el idioma activo (es/en).
    List<MenuMenuItenTO> listaAccesos(String perfil, Properties idioma);

    // Indexa los accesos por id ("mipaciente" -> ["/view/main_paciente.fxml",
    // "Gestión Pacientes", "T"]) para que el listener del MenuItem sepa qué
    // FXML cargar al hacer click.
    Map<String, String[]> accesosAutorizados(List<MenuMenuItenTO> accesos);
}
