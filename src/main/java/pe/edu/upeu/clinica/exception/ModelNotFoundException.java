package pe.edu.upeu.clinica.exception;

// Excepción no chequeada que se lanza cuando un findById no encuentra un registro.
// La levantan CrudGenericoServiceImp.findById y .update/.delete antes de operar.
public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(String message) {
        super(message);
    }
}
