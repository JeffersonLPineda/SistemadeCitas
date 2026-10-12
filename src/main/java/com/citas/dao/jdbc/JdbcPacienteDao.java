package com.citas.dao.jdbc;

import com.citas.dao.PacienteDao;
import com.citas.model.Paciente;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcPacienteDao implements PacienteDao {

    private static final String SELECT = "SELECT " + Mapeo.COLUMNAS_PACIENTE + " FROM paciente p";
    private final Jdbc jdbc;

    public JdbcPacienteDao(DataSource fuente) {
        this.jdbc = new Jdbc(fuente);
    }

    @Override
    public String siguienteCodigo() {
        return String.format("PAC-%04d", jdbc.siguienteValor("seq_paciente"));
    }

    @Override
    public void guardar(Paciente p) {
        jdbc.ejecutar("INSERT INTO paciente (codigo, nombre, telefono, correo, direccion, estado) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (codigo) DO UPDATE SET nombre = EXCLUDED.nombre, telefono = EXCLUDED.telefono, "
                + "correo = EXCLUDED.correo, direccion = EXCLUDED.direccion, estado = EXCLUDED.estado", ps -> {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getCorreo());
            ps.setString(5, p.getDireccion());
            ps.setString(6, p.getEstado().name());
        });
    }

    @Override
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String c = codigo.trim();
        return jdbc.uno(SELECT + " WHERE p.codigo = ?", ps -> ps.setString(1, c), Mapeo::paciente);
    }

    @Override
    public List<Paciente> listar() {
        return jdbc.listar(SELECT + " ORDER BY p.codigo", Jdbc.SIN_PARAMETROS, Mapeo::paciente);
    }
}
