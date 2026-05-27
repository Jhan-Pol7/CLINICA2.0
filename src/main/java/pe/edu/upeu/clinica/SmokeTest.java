package pe.edu.upeu.clinica;

import pe.edu.upeu.clinica.config.DatabaseConfig;
import pe.edu.upeu.clinica.dto.MenuMenuItenTO;
import pe.edu.upeu.clinica.model.Usuario;
import pe.edu.upeu.clinica.repository.UsuarioRepository;
import pe.edu.upeu.clinica.service.IMenuMenuItemDao;
import pe.edu.upeu.clinica.service.IUsuarioService;
import pe.edu.upeu.clinica.service.impl.MenuMenuItemDaoImp;
import pe.edu.upeu.clinica.service.impl.UsuarioServiceImp;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Verificación rápida de Fase 1 sin abrir JavaFX:
 *   1. Inicializa la BD H2 con el schema y datos semilla.
 *   2. Instancia manualmente UsuarioRepository + UsuarioService + MenuMenuItemDaoImp
 *      (no se usa AppContext porque crearía controllers JavaFX y requeriría toolkit).
 *   3. Prueba el login de cada uno de los 4 usuarios semilla.
 *   4. Imprime el menú dinámico que correspondería a cada perfil.
 *   5. Verifica que credenciales inválidas devuelven Optional vacío.
 *
 * Ejecutar:  mvn -q -B exec:java -Dexec.mainClass="pe.edu.upeu.clinica.SmokeTest"
 */
public class SmokeTest {

    public static void main(String[] args) {
        try {
            DatabaseConfig.init();

            UsuarioRepository repo = new UsuarioRepository();
            IUsuarioService us = new UsuarioServiceImp(repo);
            IMenuMenuItemDao md = new MenuMenuItemDaoImp();

            String[][] credenciales = {
                    {"admin",     "admin123"},
                    {"recep",     "recep123"},
                    {"doc.ramos", "medico123"},
                    {"enf.lopez", "enfer123"}
            };

            int ok = 0;
            for (String[] c : credenciales) {
                Optional<Usuario> u = us.loginUsuario(c[0], c[1]);
                if (u.isEmpty()) {
                    System.err.println("FAIL  Login para " + c[0]);
                    continue;
                }
                ok++;
                Usuario user = u.get();
                System.out.println("OK    Login " + user.getUsuario()
                        + "  perfil=" + user.getIdPerfil().getNombre()
                        + "  idReferencia=" + user.getIdReferencia());

                List<MenuMenuItenTO> accesos = md.listaAccesos(user.getIdPerfil().getNombre(), new Properties());
                System.out.println("      Items del menu (" + accesos.size() + "):");
                for (MenuMenuItenTO m : accesos) {
                    System.out.println("        [" + m.getMenunombre() + "] "
                            + m.getMenuitemnombre() + "  -->  " + m.getRutaFile());
                }
            }

            Optional<Usuario> bad = us.loginUsuario("admin", "claveMala");
            System.out.println(bad.isEmpty()
                    ? "OK    Login invalido rechazado"
                    : "FAIL  Login invalido fue aceptado (no deberia)");

            System.out.println();
            System.out.println("=========================");
            System.out.println(" Logins OK: " + ok + " / 4");
            System.out.println("=========================");

        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
