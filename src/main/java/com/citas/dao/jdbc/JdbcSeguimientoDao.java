package com.citas.dao.jdbc;

import com.citas.dao.DaoException;
import com.citas.dao.SeguimientoDao;
import com.citas.exception.GestionCitasException;
import com.citas.model.Seguimiento;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class JdbcSeguimientoDao implements SeguimientoDao {

    private static final String SELECT =
            "SELECT s.codigo AS seg_codigo, s.fecha AS seg_fecha, s.observaciones AS seg_obs, "
            + "s.recomendaciones AS seg_rec, s.proxima_accion AS seg_prox, " + Mapeo.COLUMNAS_CITA
            + " FROM seguimiento s JOIN cita c ON c.codigo = s.codigo_cita "
            + "JOIN paciente p ON p.codigo = c.codigo_paciente JOIN profesional pr ON pr.codigo = c.codigo_profesional";
    private static final String ORDEN = " ORDER BY s.fecha, s.codigo";
    private final Jdbc jdbc;

    public JdbcSeguimientoDao(DataSource fuente) {
        this.jdbc = new Jdbc(fuente);
    }

    @Override
    public String siguienteCodigo() {
        return String.format("SEG-%04d", jdbc.siguienteValor("seq_seguimiento"));
    }

    @Override
    public void guardar(Seguimiento s) {
        jdbc.ejecutar("INSERT INTO seguimiento (codigo, codigo_cita, fecha, observaciones, recomendaciones, proxima_accion) "
                + "VALUES (?, ?, ?, ?, ?, ?)", ps -> {
            ps.setString(1, s.getCodigo());
            ps.setString(2, s.getCita().getCodigo());
            ps.setObject(3, s.getFecha());
            ps.setString(4, s.getObservaciones());
            ps.setString(5, s.getRecomendaciones());
            ps.setString(6, s.getProximaAccion());
        });
    }

    @Override
    public List<Seguimiento> listar() {
        return jdbc.listar(SELECT + ORDEN, Jdbc.SIN_PARAMETROS, JdbcSeguimientoDao::mapear);
    }

    @Override
    public List<Seguimiento> listarPorCita(String codigoCita) {
        return jdbc.listar(SELECT + " WHERE c.codigo = ?" + ORDEN, ps -> ps.setString(1, codigoCita), JdbcSeguimientoDao::mapear);
    }

    @Override
    public List<Seguimiento> listarPorPaciente(String codigoPaciente) {
        return jdbc.listar(SELECT + " WHERE p.codigo = ?" + ORDEN, ps -> ps.setString(1, codigoPaciente), JdbcSeguimientoDao::mapear);
    }

    private static Seguimiento mapear(ResultSet rs) throws SQLException {
        try {
            return new Seguimiento(rs.getString("seg_codigo"), Mapeo.cita(rs),
                    rs.getObject("seg_fecha", java.time.LocalDate.class), rs.getString("seg_obs"),
                    rs.getString("seg_rec"), rs.getString("seg_prox"));
        } catch (GestionCitasException e) {
            throw new DaoException("Seguimiento con datos inválidos en la base de datos", e);
        }
    }
}
