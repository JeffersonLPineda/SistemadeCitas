package com.citas.exception;

/** Excepcion base (verificada) de todas las reglas de negocio del sistema. */
public class GestionCitasException extends Exception {
    private static final long serialVersionUID = 1L;

    public GestionCitasException(String mensaje) {
        super(mensaje);
    }
}
