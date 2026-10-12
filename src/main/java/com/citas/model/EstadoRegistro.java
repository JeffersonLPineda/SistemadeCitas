package com.citas.model;

/** Estado de un usuario, paciente o profesional. */
public enum EstadoRegistro {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String etiqueta;

    EstadoRegistro(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
