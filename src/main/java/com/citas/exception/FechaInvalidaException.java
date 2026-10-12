package com.citas.exception;

/** La fecha/hora de la cita es anterior al momento actual o no es valida. */
public class FechaInvalidaException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public FechaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
