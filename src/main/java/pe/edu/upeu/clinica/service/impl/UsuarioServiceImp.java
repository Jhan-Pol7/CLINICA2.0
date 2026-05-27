package pe.edu.upeu.clinica.service.impl;

import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.repository.ICrudGenericoRepository;
import pe.edu.upeu.clinica.repository.UsuarioRepository;
import pe.edu.upeu.clinica.service.IUsuarioService;

import java.util.Optional;

// Servicio de Usuario: delega el CRUD al genérico y delega login/búsqueda
// al UsuarioRepository (que ya implementa las queries con JOIN a Perfil).
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {

    // Repositorio inyectado por constructor (AppContext lo arma).
    private final UsuarioRepository repo;

    public UsuarioServiceImp(UsuarioRepository repo) { this.repo = repo; }

    // Exponer el repo al padre genérico.
    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() { return repo; }

    @Override
    public Optional<Usuario> loginUsuario(String usuario, String clave) {
        return repo.findByUsuarioAndClave(usuario, clave);
    }

    @Override
    public Optional<Usuario> buscarUsuario(String usuario) {
        return repo.buscarUsuario(usuario);
    }
}
