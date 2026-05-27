package pe.edu.upeu.clinica.utils;

import com.github.anastaciocintra.output.PrinterOutputStream;

import javax.print.PrintService;
import java.io.IOException;

// Singleton que busca y guarda la referencia a la impresora térmica
// POS-80-Series para imprimir tickets en formato ESC/POS.
// Si no encuentra la impresora, lanza IOException y la app puede mostrar
// el ticket como PDF en su lugar.
public class PrinterManager {
    private static PrinterManager instance;
    private PrintService printService;
    // Texto que se busca dentro del nombre de las impresoras del sistema.
    private final String printerNameR = "pos-80-series";

    private PrinterManager() throws IOException { initializePrinter(); }

    // Devuelve la única instancia (singleton thread-safe).
    public static synchronized PrinterManager getInstance() throws IOException {
        if (instance == null) instance = new PrinterManager();
        return instance;
    }

    // Recorre las impresoras instaladas en el sistema y selecciona la que
    // contenga "pos-80-series" en su nombre.
    private void initializePrinter() throws IOException {
        String[] printerNames = PrinterOutputStream.getListPrintServicesNames();
        System.out.println("Impresoras disponibles:");
        for (String pn : printerNames) System.out.println(" - " + pn);
        String targetPrinter = null;
        for (String pn : printerNames) {
            if (pn.toLowerCase().contains(printerNameR.toLowerCase())) {
                targetPrinter = pn;
                break;
            }
        }
        if (targetPrinter == null) {
            throw new IOException("No se encontró ninguna impresora 'POS-80-Series'.");
        }
        System.out.println("Impresora seleccionada: " + targetPrinter);
        this.printService = PrinterOutputStream.getPrintServiceByName(targetPrinter);
    }
    public PrintService getPrintService() { return printService; }
}
