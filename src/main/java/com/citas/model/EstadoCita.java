package com.citas.model;

/**
 * Estados de una cita y sus transiciones validas:
 * PROGRAMADA -> CONFIRMADA -> ATENDIDA   y   PROGRAMADA/CONFIRMADA -> CANCELADA.
 */
public enum EstadoCita {
    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean puedePasarA(EstadoCita destino) {
        switch (this) {
            case PROGRAMADA:
                return destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA:
                return destino == ATENDIDA || destino == CANCELADA;
            default:
                return false; // ATENDIDA y CANCELADA son estados finales
        }
    }
}
