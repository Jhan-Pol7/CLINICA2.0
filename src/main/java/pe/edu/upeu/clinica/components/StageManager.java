package pe.edu.upeu.clinica.components;

import javafx.stage.Stage;

// Guarda la referencia al Stage principal de la aplicación para que cualquier
// controller pueda acceder a la ventana actual sin tener que pasarla como parámetro.
// Lo setea LoginController una vez que el usuario se autentica.
public class StageManager {
    private static Stage primaryStage;
    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static Stage getPrimaryStage() { return primaryStage; }
}
