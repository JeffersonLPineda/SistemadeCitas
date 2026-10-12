package com.citas.dao;

import java.sql.SQLException;

/** Error de infraestructura en la capa de datos (BD caida, SQL invalido, dato corrupto...). No es una regla de negocio. */
public class DaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String sqlState;

    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.sqlState = causa instanceof SQLException ? ((SQLException) causa).getSQLState() : null;
    }

    /** Verdadero si la BD rechazo la operacion por una restriccion UNIQUE (SQLSTATE 23505). */
    public boolean esViolacionUnica() {
        return "23505".equals(sqlState);
    }

    /** Texto completo del error original (incluye el nombre de la restriccion violada). */
    public String detalle() {
        return getCause() != null ? String.valueOf(getCause().getMessage()) : String.valueOf(getMessage());
    }
}
