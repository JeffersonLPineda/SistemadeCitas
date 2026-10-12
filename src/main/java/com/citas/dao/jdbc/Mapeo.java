package com.citas.dao.jdbc;

import com.citas.dao.DaoException;
import com.citas.exception.GestionCitasException;
import com.citas.model.Cita;
import com.citas.model.Disponibilidad;
import com.citas.model.EstadoCita;
import com.citas.model.EstadoRegistro;
import com.citas.model.Paciente;
import com.citas.model.Profesional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/** Conversion de filas (ResultSet) a objetos del dominio. Las columnas usan alias pac_*, pro_*, cit_*. */
final class Mapeo {

    static final String COLUMNAS_PACIENTE =
            "p.codigo AS pac_codigo, p.nombre AS pac_nombre, p.telefono AS pac_telefono, "
            + "p.correo AS pac_correo, p.direccion AS pac_direccion, p.estado AS pac_estado";

    static final String COLUMNAS_PROFESIONAL =
            "pr.codigo AS pro_codigo, pr.nombre AS pro_nombre, pr.especialidad AS pro_especialidad, "
            + "pr.dias AS pro_dias, pr.hora_inicio AS pro_inicio, pr.hora_fin AS pro_fin, pr.estado AS pro_estado";

    static final String COLUMNAS_CITA =
            "c.codigo AS cit_codigo, c.fecha AS cit_fecha, c.hora AS cit_hora, c.motivo AS cit_motivo, "
            + "c.estado AS cit_estado, " + COLUMNAS_PACIENTE + ", " + COLUMNAS_PROFESIONAL;

    static final String DESDE_CITA =
            "cita c JOIN paciente p ON p.codigo = c.codigo_paciente "
            + "JOIN profesional pr ON pr.codigo = c.codigo_profesional";

    private Mapeo() {
    }

    static Paciente paciente(ResultSet rs) throws SQLException {
        try {
            Paciente p = new Paciente(rs.getString("pac_codigo"), rs.getString("pac_nombre"),
                    rs.getString("pac_telefono"), rs.getString("pac_correo"), rs.getString("pac_direccion"));
            p.setEstado(EstadoRegistro.valueOf(rs.getString("pac_estado")));
            return p;
        } catch (GestionCitasException e) {
            throw new DaoException("Paciente con datos inválidos en la base de datos", e);
        }
    }

    static Profesional profesional(ResultSet rs) throws SQLException {
        try {
            Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
            for (String d : rs.getString("pro_dias").split(",")) {
                dias.add(DayOfWeek.valueOf(d.trim()));
            }
            Disponibilidad disp = new Disponibilidad(dias, rs.getObject("pro_inicio", LocalTime.class),
                    rs.getObject("pro_fin", LocalTime.class));
            Profesional p = new Profesional(rs.getString("pro_codigo"), rs.getString("pro_nombre"),
                    rs.getString("pro_especialidad"), disp);
            p.setEstado(EstadoRegistro.valueOf(rs.getString("pro_estado")));
            return p;
        } catch (GestionCitasException e) {
            throw new DaoException("Profesional con datos inválidos en la base de datos", e);
        }
    }

    static Cita cita(ResultSet rs) throws SQLException {
        try {
            return Cita.restaurar(rs.getString("cit_codigo"), paciente(rs), profesional(rs),
                    rs.getObject("cit_fecha", java.time.LocalDate.class), rs.getObject("cit_hora", LocalTime.class),
                    rs.getString("cit_motivo"), EstadoCita.valueOf(rs.getString("cit_estado")));
        } catch (GestionCitasException e) {
            throw new DaoException("Cita con datos inválidos en la base de datos", e);
        }
    }
}
