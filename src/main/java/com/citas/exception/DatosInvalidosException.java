package com.citas.exception;

/** Datos de entrada incompletos o con formato incorrecto (campos vacios, correo invalido, etc.). */
public class DatosInvalidosException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
