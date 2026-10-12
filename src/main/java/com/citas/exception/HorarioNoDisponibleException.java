package com.citas.exception;

/** El horario solicitado esta ocupado o fuera de la disponibilidad del profesional. */
public class HorarioNoDisponibleException extends GestionCitasException {
    private static final long serialVersionUID = 1L;

    public HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
