package pe.edu.upeu.clinica.components;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

// Utilidad de validación visual para campos de formulario.
// marcarError()  → aplica borde rojo + Tooltip de advertencia al campo.
// limpiarCampo() → quita el borde y el Tooltip, restaurando el aspecto normal.
// Las constantes ESTILO_ERROR / ESTILO_NORMAL son accesibles desde cualquier
// controller para aplicar estilos manualmente si se prefiere.
public class ToltipCustom {
    // Estilo CSS para campo con error (borde rojo de 2px).
    public static final String ESTILO_ERROR  = "-fx-border-color: #e53935; -fx-border-width: 2px; -fx-border-radius: 3px;";
    // Estilo vacío = aspecto por defecto del tema activo.
    public static final String ESTILO_NORMAL = "";

    // Aplica borde rojo al campo y adjunta un Tooltip con el mensaje de error.
    // El Tooltip aparece casi de inmediato (100 ms de retardo) y se mantiene
    // visible 10 segundos para que el usuario pueda leerlo con calma.
    public void marcarError(Control campo, String mensaje) {
        campo.setStyle(ESTILO_ERROR);
        Tooltip tooltip = new Tooltip("⚠  " + mensaje);
        tooltip.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 10 6 10; -fx-background-radius: 4;");
        tooltip.setShowDelay(Duration.millis(100));    // aparece rápido
        tooltip.setHideDelay(Duration.millis(200));    // desaparece suave
        tooltip.setShowDuration(Duration.seconds(10)); // tiempo máximo visible
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
        Tooltip.install(campo, tooltip);
    }

    // Elimina el borde de error y quita el Tooltip, dejando el campo
    // en su estado visual normal (listo para el siguiente intento del usuario).
    public void limpiarCampo(Control campo) {
        campo.setStyle(ESTILO_NORMAL);
        Tooltip.install(campo, null);
    }
}
