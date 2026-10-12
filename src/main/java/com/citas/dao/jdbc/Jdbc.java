package com.citas.dao.jdbc;

import com.citas.dao.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Ayudante minimo de JDBC: abre/cierra conexiones del pool y siempre usa PreparedStatement (sin concatenar datos). */
final class Jdbc {

    interface Preparador {
        void aplicar(PreparedStatement ps) throws SQLException;
    }

    interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    static final Preparador SIN_PARAMETROS = ps -> { };

    private final DataSource fuente;

    Jdbc(DataSource fuente) {
        this.fuente = fuente;
    }

    <T> List<T> listar(String sql, Preparador preparador, Mapeador<T> mapeador) {
        try (Connection c = fuente.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            preparador.aplicar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapeador.mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al consultar la base de datos", e);
        }
    }

    <T> Optional<T> uno(String sql, Preparador preparador, Mapeador<T> mapeador) {
        List<T> lista = listar(sql, preparador, mapeador);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** INSERT/UPDATE/DELETE. Devuelve las filas afectadas. */
    int ejecutar(String sql, Preparador preparador) {
        try (Connection c = fuente.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            preparador.aplicar(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al modificar la base de datos", e);
        }
    }

    long siguienteValor(String secuencia) {
        return uno("SELECT nextval('" + secuencia + "')", SIN_PARAMETROS, rs -> rs.getLong(1))
                .orElseThrow(() -> new DaoException("La secuencia " + secuencia + " no devolvio valor", null));
    }
}
