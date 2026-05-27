package pe.edu.upeu.clinica.components;

// DTO simple usado por TableViewHelper para describir una columna:
// "field" es el nombre del atributo del modelo (ej: "nombres" o "medico.nombres")
// y "width" es el ancho preferido en píxeles.
public class ColumnInfo {
    private String field;
    private Double width;
    public ColumnInfo(String field, Double width) { this.field = field; this.width = width; }
    public String getField() { return field; }
    public Double getWidth() { return width; }
}
