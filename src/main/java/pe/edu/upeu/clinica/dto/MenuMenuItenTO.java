package pe.edu.upeu.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa UN item del menú dinámico de MainGui (un MenuItem
// dentro de un Menu del MenuBar). Lo produce MenuMenuItemDaoImp.listaAccesos
// y lo consume MainGuiController.graficarMenus().
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuMenuItenTO {
    // Identificador lógico ("paciente", "cita", "triaje"...). MainGuiController
    // le antepone "mi" para formar el fx:id del MenuItem y "m" para el del Menu.
    String idNombreObj;
    String rutaFile;       // ruta del FXML que se abrirá ("/view/main_xxx.fxml")
    String menunombre;     // texto del Menu padre ("Personas", "Atención", ...)
    String menuitemnombre; // texto del MenuItem ("Pacientes", "Registrar Cita", ...)
    String nombreTab;      // título del Tab cuando se abra en el TabPane central
    String tipoTab;        // "T" = abrir en Tab; "S" = redireccionar (ej. salir)
}
