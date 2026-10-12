package com.citas.dao.jdbc;

import com.citas.dao.ProfesionalDao;
import com.citas.model.Profesional;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class JdbcProfesionalDao implements ProfesionalDao {

    private static final String SELECT = "SELECT " + Mapeo.COLUMNAS_PROFESIONAL + " FROM profesional pr";
    private final Jdbc jdbc;

    public JdbcProfesionalDao(DataSource fuente) {
        this.jdbc = new Jdbc(fuente);
    }

    @Override
    public String siguienteCodigo() {
        return String.format("PRO-%04d", jdbc.siguienteValor("seq_profesional"));
    }

    @Override
    public void guardar(Profesional p) {
        String dias = p.getDisponibilidad().getDias().stream().map(DayOfWeek::name).collect(Collectors.joining(","));
        jdbc.ejecutar("INSERT INTO profesional (codigo, nombre, especialidad, dias, hora_inicio, hora_fin, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (codigo) DO UPDATE SET nombre = EXCLUDED.nombre, especialidad = EXCLUDED.especialidad, "
                + "dias = EXCLUDED.dias, hora_inicio = EXCLUDED.hora_inicio, hora_fin = EXCLUDED.hora_fin, "
                + "estado = EXCLUDED.estado", ps -> {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getEspecialidad());
            ps.setString(4, dias);
            ps.setObject(5, p.getDisponibilidad().getInicio());
            ps.setObject(6, p.getDisponibilidad().getFin());
            ps.setString(7, p.getEstado().name());
        });
    }

    @Override
    public Optional<Profesional> buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String c = codigo.trim();
        return jdbc.uno(SELECT + " WHERE pr.codigo = ?", ps -> ps.setString(1, c), Mapeo::profesional);
    }

    @Override
    public List<Profesional> listar() {
        return jdbc.listar(SELECT + " ORDER BY pr.codigo", Jdbc.SIN_PARAMETROS, Mapeo::profesional);
    }
}
