package pe.edu.upeu.clinica.repository;

import pe.edu.upeu.clinica.enums.Sexo;
import pe.edu.upeu.clinica.model.Paciente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

// Repositorio del Paciente. Maneja LocalDate <-> java.sql.Date para fechaNacimiento
// y Enum Sexo <-> String (.name()) para la columna sexo.
public class PacienteRepository extends AbstractJpaRepository<Paciente, Long> {

    @Override protected String getTableName() { return "upeu_paciente"; }
    @Override protected String getPkColumn()  { return "id_paciente";   }

    // Búsqueda por DNI, usada por MainCitaController para autocompletar
    // datos del paciente al registrar una cita.
    public Optional<Paciente> findByDni(String dni) {
        return executeQueryOne("SELECT * FROM upeu_paciente WHERE dni = ?", dni);
    }

    @Override
    protected Paciente insert(Connection connection, Paciente p) throws SQLException {
        long id = executeInsertGetKey(connection,
                "INSERT INTO upeu_paciente(dni, nombres, apellidos, telefono, fecha_nacimiento, sexo, direccion, email) VALUES(?,?,?,?,?,?,?,?)",
                p.getDni(), p.getNombres(), p.getApellidos(), p.getTelefono(),
                // LocalDate -> java.sql.Date; null si no se ingresó
                p.getFechaNacimiento() == null ? null : Date.valueOf(p.getFechaNacimiento()),
                // Enum -> String (la BD guarda MASCULINO/FEMENINO)
                p.getSexo() == null ? null : p.getSexo().name(),
                p.getDireccion(), p.getEmail());
        p.setIdPaciente(id);
        return p;
    }

    @Override
    protected Paciente updateRow(Connection connection, Paciente p) throws SQLException {
        executeUpdate(connection,
                "UPDATE upeu_paciente SET dni=?, nombres=?, apellidos=?, telefono=?, fecha_nacimiento=?, sexo=?, direccion=?, email=? WHERE id_paciente=?",
                p.getDni(), p.getNombres(), p.getApellidos(), p.getTelefono(),
                p.getFechaNacimiento() == null ? null : Date.valueOf(p.getFechaNacimiento()),
                p.getSexo() == null ? null : p.getSexo().name(),
                p.getDireccion(), p.getEmail(),
                p.getIdPaciente());
        return p;
    }

    @Override
    protected Paciente mapRow(ResultSet rs) throws SQLException {
        // Conversión inversa: java.sql.Date -> LocalDate, String -> Sexo.
        Date fn = rs.getDate("fecha_nacimiento");
        String sexoStr = rs.getString("sexo");
        return Paciente.builder()
                .idPaciente(rs.getLong("id_paciente"))
                .dni(rs.getString("dni"))
                .nombres(rs.getString("nombres"))
                .apellidos(rs.getString("apellidos"))
                .telefono(rs.getString("telefono"))
                .fechaNacimiento(fn == null ? null : fn.toLocalDate())
                .sexo(sexoStr == null ? null : Sexo.valueOf(sexoStr))
                .direccion(rs.getString("direccion"))
                .email(rs.getString("email"))
                .build();
    }
}
