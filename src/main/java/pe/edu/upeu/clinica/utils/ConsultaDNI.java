package pe.edu.upeu.clinica.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pe.edu.upeu.clinica.dto.PersonaDto;

import java.io.IOException;

// Servicio que consulta la página web eldni.com para obtener
// los nombres y apellidos a partir de un número de DNI peruano.
// Se usa para autocompletar los datos del paciente o médico cuando
// se ingresa el DNI manualmente.
public class ConsultaDNI {

    // Devuelve un PersonaDto con los datos encontrados.
    // Si el DNI no existe o hay error de red, devuelve un DTO vacío.
    public PersonaDto consultarDNI(String dni) {
        PersonaDto personaDto = new PersonaDto();
        String url = "https://eldni.com/pe/buscar-datos-por-dni";
        try {
            // Primera petición GET para obtener el token CSRF requerido por la página.
            Connection.Response getResponse = Jsoup.connect(url)
                    .method(Connection.Method.GET).execute();
            Document doc = getResponse.parse();
            String token = doc.select("input[name=_token]").attr("value");
            // Petición POST con el DNI y el token para obtener los datos.
            Connection.Response postResponse = Jsoup.connect(url)
                    .cookies(getResponse.cookies())
                    .data("_token", token)
                    .data("dni", dni)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true).execute();
            Document resultDoc = Jsoup.parse(postResponse.body());
            // Se extrae la primera fila de la tabla de resultados.
            Element fila = resultDoc.select("table tbody tr").first();
            if (fila != null) {
                Elements celdas = fila.select("td");
                personaDto.setDni(celdas.get(0).text());
                personaDto.setNombre(celdas.get(1).text());
                personaDto.setApellidoPaterno(celdas.get(2).text());
                personaDto.setApellidoMaterno(celdas.get(3).text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return personaDto;
    }
}
