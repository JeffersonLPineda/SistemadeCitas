package com.citas.dao.jdbc;

import com.citas.dao.CitaDao;
import com.citas.dao.DaoException;
import com.citas.exception.CitaNoEncontradaException;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.GestionCitasException;
import com.citas.exception.HorarioNoDisponibleException;
import com.citas.model.Cita;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcCitaDao implements CitaDao {

    private static final String SELECT = "SELECT " + Mapeo.COLUMNAS_CITA + " FROM " + Mapeo.DESDE_CITA;
    private static final String ORDEN = " ORDER BY c.fecha, c.hora, c.codigo";
    private final Jdbc jdbc;

    public JdbcCitaDao(DataSource fuente) {
        this.jdbc = new Jdbc(fuente);
    }

    @Override
    public String siguienteCodigo() {
        return String.format("CIT-%04d", jdbc.siguienteValor("seq_cita"));
    }

    @Override
    public void guardar(Cita c) throws GestionCitasException {
        try {
            jdbc.ejecutar("INSERT INTO cita (codigo, codigo_paciente, codigo_profesional, fecha, hora, motivo, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)", ps -> {
                ps.setString(1, c.getCodigo());
                ps.setString(2, c.getPaciente().getCodigo());
                ps.setString(3, c.getProfesional().getCodigo());
                ps.setObject(4, c.getFecha());
                ps.setObject(5, c.getHora());
                ps.setString(6, c.getMotivo());
                ps.setString(7, c.getEstado().name());
            });
        } catch (DaoException e) {
            if (e.esViolacionUnica()) {
                if (e.detalle().contains("ux_cita_profesional_horario")) {
                    throw new HorarioNoDisponibleException("El profesional ya tiene una cita el "
                            + c.getFecha() + " a las " + c.getHora() + ".");
                }
                throw new DatosInvalidosException("El código de cita " + c.getCodigo() + " ya existe (los códigos deben ser únicos).");
            }
            throw e;
        }
    }

    @Override
    public void actualizar(Cita c) throws CitaNoEncontradaException {
        int filas = jdbc.ejecutar("UPDATE cita SET estado = ? WHERE codigo = ?", ps -> {
            ps.setString(1, c.getEstado().name());
            ps.setString(2, c.getCodigo());
        });
        if (filas == 0) {
            throw new CitaNoEncontradaException(c.getCodigo());
        }
    }

    @Override
    public Optional<Cita> buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String cod = codigo.trim();
        return jdbc.uno(SELECT + " WHERE c.codigo = ?", ps -> ps.setString(1, cod), Mapeo::cita);
    }

    @Override
    public List<Cita> listar() {
        return jdbc.listar(SELECT + ORDEN, Jdbc.SIN_PARAMETROS, Mapeo::cita);
    }

    @Override
    public List<Cita> listarPorPaciente(String codigoPaciente) {
        return jdbc.listar(SELECT + " WHERE c.codigo_paciente = ?" + ORDEN, ps -> ps.setString(1, codigoPaciente), Mapeo::cita);
    }

    @Override
    public List<Cita> listarPorProfesional(String codigoProfesional) {
        return jdbc.listar(SELECT + " WHERE c.codigo_profesional = ?" + ORDEN, ps -> ps.setString(1, codigoProfesional), Mapeo::cita);
    }
}
