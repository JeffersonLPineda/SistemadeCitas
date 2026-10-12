package com.citas.exception;

/** El usuario autenticado no tiene permiso para realizar la accion solicitada. */
public class AccesoDenegadoException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
