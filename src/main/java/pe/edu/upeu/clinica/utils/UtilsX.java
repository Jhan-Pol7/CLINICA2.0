package pe.edu.upeu.clinica.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

// Utilidades para cargar archivos de idioma e imagenes.
// Primero busca el archivo externo en /language; si no existe, lo carga del classpath.
// Los .properties se leen como UTF-8 para que las tildes y la "ñ" se muestren bien.
public class UtilsX {

    // Devuelve la URL de un recurso del classpath (img, css, etc.).
    public URL getFile(String ruta) { return this.getClass().getResource("/" + ruta); }

    // Construye una ruta absoluta a un archivo dentro de una carpeta externa al jar.
    public File getFileExterno(String carpeta, String filex) {
        File newFolder = new File(carpeta);
        String ruta = newFolder.getAbsolutePath();
        Path CAMINO = Paths.get(ruta + "/" + filex);
        return CAMINO.toFile();
    }

    // Devuelve la carpeta externa como File (usada para listar archivos).
    public File getFolderExterno(String carpeta) {
        File newFolder = new File(carpeta);
        String ruta = newFolder.getAbsolutePath();
        Path CAMINO = Paths.get(ruta + "/");
        return CAMINO.toFile();
    }

    // Lee el archivo idiomas-XX.properties con codificación UTF-8.
    // Sin esto, las tildes ("Atención") se mostrarían como caracteres raros en pantalla.
    public Properties detectLanguage(String idioma) {
        Properties props = new Properties();
        String fileName = "idiomas-" + idioma + ".properties";
        File externo = getFileExterno("language", fileName);
        if (externo.exists()) {
            // Carpeta externa al jar (sobrescribe los del classpath si existe).
            try (Reader in = new InputStreamReader(new FileInputStream(externo), StandardCharsets.UTF_8)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Si no hay archivo externo, lo busca dentro del jar.
        try (InputStream in = getClass().getResourceAsStream("/language/" + fileName)) {
            if (in != null) {
                try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    props.load(r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    // Lee el idioma guardado por el usuario en el archivo de configuración externo.
    // Si no existe, devuelve "es" por defecto.
    public String readLanguageFile() {
        Properties props = new Properties();
        String idioma = "es";
        File f = getFileExterno("language", "ClinicaMasCercaDeDios.properties");
        if (!f.exists()) return idioma;
        try (Reader in = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            props.load(in);
            idioma = props.getProperty("clinica.idioma", "es");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return idioma;
    }
}
