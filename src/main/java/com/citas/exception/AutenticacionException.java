package com.citas.exception;

/** Credenciales incorrectas o usuario inactivo. */
public class AutenticacionException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
