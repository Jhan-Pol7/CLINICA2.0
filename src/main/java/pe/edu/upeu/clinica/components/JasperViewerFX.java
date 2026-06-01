package pe.edu.upeu.clinica.components;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Visor JasperFX simple (basado en el de Gustavo Fragoso).
 * Usa botones con texto para evitar dependencia de iconos en el classpath.
 */
public class JasperViewerFX extends Dialog<Void> {

    private Button btnPrint;
    private Button btnSave;
    private Button btnBackPage;
    private Button btnFirstPage;
    private Button btnNextPage;
    private Button btnLastPage;
    private Button btnZoomIn;
    private Button btnZoomOut;
    private DialogPane dialogPane;
    private ImageView report;
    private Label lblReportPages;
    private Stage view;
    private TextField txtPage;

    private JasperPrint jasperPrint;

    private final SimpleIntegerProperty currentPage;
    private int imageHeight = 0;
    private int imageWidth = 0;
    private int reportPages = 0;

    public JasperViewerFX() {
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);

        dialogPane = getDialogPane();
        dialogPane.setContent(createContentPane());
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.lookupButton(ButtonType.CLOSE).setVisible(false);

        currentPage = new SimpleIntegerProperty(this, "currentPage", 1);
    }

    private BorderPane createContentPane() {
        btnPrint     = new Button("Imprimir");
        btnSave      = new Button("Guardar");
        btnBackPage  = new Button("◀");
        btnFirstPage = new Button("◀◀");
        btnNextPage  = new Button("▶");
        btnLastPage  = new Button("▶▶");
        btnZoomIn    = new Button("+");
        btnZoomOut   = new Button("-");

        btnBackPage.setOnAction(e -> renderPage(getCurrentPage() - 1));
        btnFirstPage.setOnAction(e -> renderPage(1));
        btnNextPage.setOnAction(e -> renderPage(getCurrentPage() + 1));
        btnLastPage.setOnAction(e -> renderPage(reportPages));
        btnZoomIn.setOnAction(e -> zoom(0.15));
        btnZoomOut.setOnAction(e -> zoom(-0.15));

        printAction();
        saveAction();

        txtPage = new TextField("1");
        txtPage.setPrefSize(50, 30);
        txtPage.setOnAction(event -> {
            try {
                int page = Integer.parseInt(txtPage.getText());
                renderPage(((page > 0 && page <= reportPages) ? page : 1));
            } catch (NumberFormatException e) {
                renderPage(1);
            }
        });

        lblReportPages = new Label("/ 1");

        HBox menu = new HBox(5);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(5));
        menu.setPrefHeight(50.0);
        menu.getChildren().addAll(btnPrint, btnSave, btnFirstPage, btnBackPage, txtPage,
                lblReportPages, btnNextPage, btnLastPage, btnZoomIn, btnZoomOut);

        report = new ImageView();
        report.setFitHeight(imageHeight);
        report.setFitWidth(imageWidth);

        Group contentGroup = new Group();
        contentGroup.getChildren().add(report);

        StackPane stack = new StackPane(contentGroup);
        stack.setAlignment(Pos.CENTER);
        stack.setStyle("-fx-background-color: gray");

        ScrollPane scroll = new ScrollPane(stack);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        BorderPane root = new BorderPane();
        root.setTop(menu);
        root.setCenter(scroll);
        root.setPrefSize(1024, 768);
        return root;
    }

    public void setCurrentPage(int pageNumber) { currentPage.set(pageNumber); }
    public int getCurrentPage() { return currentPage.get(); }
    public SimpleIntegerProperty currentPageProperty() { return currentPage; }

    private void printAction() {
        btnPrint.setOnAction(event -> {
            try {
                JasperPrintManager.printReport(jasperPrint, true);
                close();
            } catch (JRException ex) { ex.printStackTrace(); }
        });
    }

    private void saveAction() {
        btnSave.setOnAction(event -> {
            ExtensionFilter pdf  = new ExtensionFilter("PDF", "*.pdf");
            ExtensionFilter html = new ExtensionFilter("HTML", "*.html");
            ExtensionFilter xml  = new ExtensionFilter("XML",  "*.xml");
            ExtensionFilter xls  = new ExtensionFilter("Excel 2007", "*.xls");
            ExtensionFilter xlsx = new ExtensionFilter("Excel 2016", "*.xlsx");
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar como");
            chooser.getExtensionFilters().addAll(pdf, html, xml, xls, xlsx);
            chooser.setSelectedExtensionFilter(pdf);
            File file = chooser.showSaveDialog(view);
            if (file != null) {
                List<String> sel = chooser.getSelectedExtensionFilter().getExtensions();
                exportTo(file, sel.get(0));
            }
        });
    }

    private void disableUnnecessaryButtons(int pageNumber) {
        boolean isFirstPage = (pageNumber == 1);
        boolean isLastPage = (pageNumber == reportPages);
        btnBackPage.setDisable(isFirstPage);
        btnFirstPage.setDisable(isFirstPage);
        btnNextPage.setDisable(isLastPage);
        btnLastPage.setDisable(isLastPage);
    }

    private void exportTo(File file, String extension) {
        switch (extension) {
            case "*.pdf":  exportToPdf(file);  break;
            case "*.html": exportToHtml(file); break;
            case "*.xml":  exportToXml(file);  break;
            case "*.xls":
            case "*.xlsx": exportToXlsx(file); break;
            default:       exportToPdf(file);
        }
    }

    public void exportToHtml(File file) {
        try { JasperExportManager.exportReportToHtmlFile(jasperPrint, file.getPath()); }
        catch (JRException ex) { ex.printStackTrace(); }
    }
    public void exportToPdf(File file) {
        try { JasperExportManager.exportReportToPdfFile(jasperPrint, file.getPath()); }
        catch (JRException ex) { ex.printStackTrace(); }
    }
    public void exportToXlsx(File file) {
        try {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
            exporter.exportReport();
        } catch (JRException ex) { ex.printStackTrace(); }
    }
    public void exportToXml(File file) {
        try { JasperExportManager.exportReportToXmlFile(jasperPrint, file.getPath(), false); }
        catch (JRException ex) { ex.printStackTrace(); }
    }

    private Image pageToImage(int pageNumber) {
        try {
            float zoom = 1.33f;
            BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, pageNumber - 1, zoom);
            WritableImage fxImage = new WritableImage(imageHeight, imageWidth);
            return SwingFXUtils.toFXImage(image, fxImage);
        } catch (JRException ex) { ex.printStackTrace(); }
        return null;
    }

    private void renderPage(int pageNumber) {
        setCurrentPage(pageNumber);
        disableUnnecessaryButtons(pageNumber);
        txtPage.setText(Integer.toString(pageNumber));
        report.setImage(pageToImage(pageNumber));
    }

    public void zoom(double factor) {
        report.setScaleX(report.getScaleX() + factor);
        report.setScaleY(report.getScaleY() + factor);
        report.setFitHeight(imageHeight + factor);
        report.setFitWidth(imageWidth + factor);
    }

    public void viewReport(String title, JasperPrint jasperPrint) {
        this.jasperPrint = jasperPrint;
        imageHeight = jasperPrint.getPageHeight() + 284;
        imageWidth = jasperPrint.getPageWidth() + 201;
        reportPages = jasperPrint.getPages().size();
        lblReportPages.setText("/ " + reportPages);
        if (reportPages > 0) renderPage(1);
        setTitle(title);
        show();
    }
}
