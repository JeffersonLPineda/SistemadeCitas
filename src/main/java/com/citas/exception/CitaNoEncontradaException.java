package com.citas.exception;

/** No existe una cita con el codigo indicado. */
public class CitaNoEncontradaException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public CitaNoEncontradaException(String codigo) {
        super("No se encontró la cita con código: " + codigo);
    }
}
