package pe.edu.upeu.clinica.service;

import pe.edu.upeu.clinica.model.Usuario;

import java.util.Optional;

// Servicio de usuarios. Hereda el CRUD genérico y agrega operaciones
// específicas para autenticación.
public interface IUsuarioService extends ICrudGenericoService<Usuario, Long> {
    // Login: valida usuario+clave. Devuelve Optional vacío si no coinciden.
    Optional<Usuario> loginUsuario(String usuario, String clave);

    // Busca un usuario por nombre (sin validar clave). Útil para validar
    // duplicados en alta o para "olvidé mi clave".
    Optional<Usuario> buscarUsuario(String usuario);
}
