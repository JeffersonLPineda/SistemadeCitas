package com.citas.exception;

/** La operacion no esta permitida para el estado actual de la cita. */
public class EstadoCitaInvalidoException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public EstadoCitaInvalidoException(String mensaje) {
        super(mensaje);
    }
}
