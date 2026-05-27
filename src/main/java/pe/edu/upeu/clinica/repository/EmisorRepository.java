package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.model.Emisor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

// Repositorio del Emisor (datos de la clínica). En la práctica hay una sola
// fila en upeu_emisor, por eso existe el método findFirst() de conveniencia.
public class EmisorRepository extends AbstractJpaRepository<Emisor, Long> {

    @Override protected String getTableName() { return "upeu_emisor"; }
    @Override protected String getPkColumn()  { return "id_emisor";   }

    // Devuelve el primer (y normalmente único) Emisor configurado.
    // Lo consume ITicketService para imprimir la cabecera del ticket/receta.
    public Optional<Emisor> findFirst() {
        return executeQueryOne("SELECT * FROM upeu_emisor LIMIT 1");
    }

    @Override
    protected Emisor insert(Connection connection, Emisor e) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_emisor(ruc,nombre_comercial,ubigeo,domicilio_fiscal,urbanizacion,departamento,provincia,distrito) VALUES(?,?,?,?,?,?,?,?)",
                e.getRuc(), e.getNombreComercial(), e.getUbigeo(), e.getDomicilioFiscal(),
                e.getUrbanizacion(), e.getDepartamento(), e.getProvincia(), e.getDistrito());
        e.setIdEmisor(id);
        return e;
    }

    @Override
    protected Emisor updateRow(Connection connection, Emisor e) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_emisor SET ruc=?,nombre_comercial=?,ubigeo=?,domicilio_fiscal=?,urbanizacion=?,departamento=?,provincia=?,distrito=? WHERE id_emisor=?",
                e.getRuc(), e.getNombreComercial(), e.getUbigeo(), e.getDomicilioFiscal(),
                e.getUrbanizacion(), e.getDepartamento(), e.getProvincia(), e.getDistrito(), e.getIdEmisor());
        return e;
    }

    @Override
    protected Emisor mapRow(ResultSet rs) throws SQLException {
        return Emisor.builder()
                .idEmisor(rs.getLong("id_emisor"))
                .ruc(rs.getString("ruc"))
                .nombreComercial(rs.getString("nombre_comercial"))
                .ubigeo(rs.getString("ubigeo"))
                .domicilioFiscal(rs.getString("domicilio_fiscal"))
                .urbanizacion(rs.getString("urbanizacion"))
                .departamento(rs.getString("departamento"))
                .provincia(rs.getString("provincia"))
                .distrito(rs.getString("distrito"))
                .build();
    }
}
