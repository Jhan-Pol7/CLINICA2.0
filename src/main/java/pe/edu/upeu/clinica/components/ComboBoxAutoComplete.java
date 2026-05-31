package pe.edu.upeu.clinica.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

import java.util.stream.Stream;

// Añade búsqueda incremental a cualquier ComboBox<T> existente.
// Al escribir letras con el combo abierto, filtra los ítems en tiempo real
// (contains, case-insensitive) y muestra un Tooltip con el texto ingresado.
// BACKSPACE borra el último carácter del filtro; ESC lo limpia por completo.
// Al cerrar el combo se restaura la lista original sin perder la selección.
public class ComboBoxAutoComplete<T> {
    private final ComboBox<T> cmb;
    private String filter = "";               // texto acumulado tecla a tecla
    private final ObservableList<T> originalItems; // copia de los ítems originales

    // Guarda los ítems originales y registra los listeners de teclado y cierre.
    public ComboBoxAutoComplete(ComboBox<T> cmb) {
        this.cmb = cmb;
        originalItems = FXCollections.observableArrayList(cmb.getItems());
        cmb.setTooltip(new Tooltip());
        cmb.setOnKeyPressed(this::handleOnKeyPressed);
        cmb.setOnHidden(this::handleOnHiding);
    }

    // Actualiza el filtro según la tecla pulsada y reconstruye la lista visible.
    //   - Letra    → agrega al filtro y filtra la lista.
    //   - BACKSPACE → elimina el último carácter del filtro y recalcula.
    //   - ESCAPE   → limpia el filtro por completo.
    public void handleOnKeyPressed(KeyEvent e) {
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();
        if (code.isLetterKey()) filter += e.getText();
        if (code == KeyCode.BACK_SPACE && filter.length() > 0) {
            filter = filter.substring(0, filter.length() - 1);
            cmb.getItems().setAll(originalItems);
        }
        if (code == KeyCode.ESCAPE) filter = "";
        if (filter.length() == 0) {
            // Sin filtro: mostrar todos los ítems y ocultar el tooltip.
            filteredList = originalItems;
            cmb.getTooltip().hide();
        } else {
            // Con filtro: conservar solo los que contienen el texto.
            Stream<T> itens = cmb.getItems().stream();
            String txtUsr = filter.toLowerCase();
            itens.filter(el -> el.toString().toLowerCase().contains(txtUsr)).forEach(filteredList::add);
            // Mostrar el texto del filtro en el tooltip junto al combo.
            cmb.getTooltip().setText(txtUsr);
            Window stage = cmb.getScene().getWindow();
            double posX = stage.getX() + cmb.getBoundsInParent().getMinX();
            double posY = stage.getY() + cmb.getBoundsInParent().getMinY();
            cmb.getTooltip().show(stage, posX, posY);
            cmb.show();
        }
        cmb.getItems().setAll(filteredList);
    }

    // Al cerrar el combo: limpia el filtro, oculta el tooltip y restaura
    // la lista original manteniendo el ítem que estaba seleccionado.
    public void handleOnHiding(Event e) {
        filter = "";
        cmb.getTooltip().hide();
        T s = cmb.getSelectionModel().getSelectedItem();
        cmb.getItems().setAll(originalItems);
        cmb.getSelectionModel().select(s);
    }
}
