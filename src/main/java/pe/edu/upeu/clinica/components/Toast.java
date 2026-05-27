package pe.edu.upeu.clinica.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

// Notificaciones flotantes tipo "toast" (mensajes temporales) que aparecen
// y desaparecen solas. Hay dos variantes: error (rojo) y éxito (verde).
public class Toast {

    // Muestra una notificación roja (error) durante "durationInMillis" milisegundos.
    // El mensaje aparece con fade-in y se oculta con fade-out.
    public static void showToast(Stage ownerStage, String message, int durationInMillis, double x, double y) {
        Label label = new Label(message);
        // Estilo: fondo rojo, texto blanco, bordes redondeados.
        label.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; "
                + "-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        label.setOpacity(0);

        // Popup flotante (no bloquea la ventana principal).
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER);
        popup.getContent().add(pane);

        popup.show(ownerStage, x, y);

        // Animaciones: aparecer (300ms) -> esperar -> desaparecer (300ms) -> cerrar.
        Timeline fadeIn = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(label.opacityProperty(), 1)));
        Timeline fadeOut = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(label.opacityProperty(), 0)));
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(durationInMillis)));
        delay.setOnFinished(event -> fadeOut.play());

        fadeIn.play();
        fadeIn.setOnFinished(event -> delay.play());
        fadeOut.setOnFinished(event -> popup.hide());
    }

    // Muestra una notificación verde (éxito) — misma lógica pero con color distinto.
    public static void showSuccess(Stage ownerStage, String message, int durationInMillis, double x, double y) {
        Label label = new Label(message);
        // Estilo: fondo verde, texto blanco, bordes redondeados.
        label.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; "
                + "-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        label.setOpacity(0);

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER);
        popup.getContent().add(pane);
        popup.show(ownerStage, x, y);

        // Animación: aparecer suavemente, esperar, desaparecer y cerrar.
        Timeline fadeIn = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(label.opacityProperty(), 1)));
        Timeline fadeOut = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(label.opacityProperty(), 0)));
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(durationInMillis)));
        delay.setOnFinished(event -> fadeOut.play());
        fadeIn.play();
        fadeIn.setOnFinished(event -> delay.play());
        fadeOut.setOnFinished(event -> popup.hide());
    }
}
