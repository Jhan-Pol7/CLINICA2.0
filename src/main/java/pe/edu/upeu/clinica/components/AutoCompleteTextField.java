package pe.edu.upeu.clinica.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.SortedSet;

// Campo de texto con autocompletado dinámico.
// Al instanciar recibe el SortedSet completo de opciones y el TextField al que
// se adjunta. Por cada pulsación de tecla, filtra los ítems cuyo toString()
// contiene el texto actual (case-insensitive) y los muestra en un ContextMenu
// flotante debajo del campo.
// Cuando el usuario elige un ítem, el texto se escribe en el campo y
// getLastSelectedObject() devuelve el objeto T seleccionado.
public class AutoCompleteTextField<T> {

    private final TextField autoCompleteTextField;
    private final SortedSet<T> entries;              // catálogo completo de opciones
    private final ContextMenu entryMenu = new ContextMenu(); // dropdown de sugerencias
    private T lastSelectedObject;                    // último ítem elegido por el usuario

    // Adjunta el listener al TextField para reaccionar a cada pulsación de tecla.
    public AutoCompleteTextField(SortedSet<T> entries, TextField autTF) {
        this.entries = entries;
        this.autoCompleteTextField = autTF;
        this.autoCompleteTextField.setOnKeyTyped(this::handleKeyReleased);
    }

    // Puente de compatibilidad: delega en accion() para poder llamarse
    // también desde código externo sin pasar el evento.
    public void handleKeyReleased(KeyEvent event) { accion(); }

    // Filtra la lista de entradas con el texto actual del campo y
    // actualiza el ContextMenu con los resultados. Si no hay resultados
    // oculta el menú para no mostrar un dropdown vacío.
    public void accion() {
        ObservableList<MenuItem> menuItems = FXCollections.observableArrayList();
        String input = this.autoCompleteTextField.getText().toLowerCase();
        entries.stream()
                .filter(e -> e.toString().toLowerCase().contains(input))
                .forEach(entry -> {
                    MenuItem item = new MenuItem(entry.toString());
                    // Al elegir un ítem: escribe el texto, guarda el objeto y cierra el menú.
                    item.setOnAction(e -> {
                        this.autoCompleteTextField.setText(entry.toString());
                        lastSelectedObject = entry;
                        entryMenu.hide();
                    });
                    menuItems.add(item);
                    entryMenu.hide();
                });
        entryMenu.getItems().setAll(menuItems);
        if (!menuItems.isEmpty()) {
            // Muestra el dropdown justo debajo del campo de texto.
            entryMenu.show(this.autoCompleteTextField, Side.BOTTOM, 0, 0);
        } else {
            entryMenu.hide();
        }
    }

    // Expone el ContextMenu por si el caller necesita controlarlo externamente.
    public ContextMenu getEntryMenu() { return entryMenu; }
    // Devuelve el objeto T de la última opción elegida (null si aún no se eligió ninguna).
    public T getLastSelectedObject() { return lastSelectedObject; }
}
