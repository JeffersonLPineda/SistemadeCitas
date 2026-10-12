package com.citas.model;

public enum Rol {
    ADMINISTRADOR("Administrador"),
    PROFESIONAL("Profesional"),
    PACIENTE("Paciente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
