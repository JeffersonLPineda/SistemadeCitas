package com.citas.model;

import com.citas.exception.DatosInvalidosException;
import com.citas.util.Validaciones;

/** Profesional que atiende citas. Invariantes: codigo, nombre, especialidad y disponibilidad no nulos. */
public class Profesional {

    private final String codigo;
    private String nombre;
    private String especialidad;
    private Disponibilidad disponibilidad;
    private EstadoRegistro estado = EstadoRegistro.ACTIVO;

    public Profesional(String codigo, String nombre, String especialidad, Disponibilidad disponibilidad)
            throws DatosInvalidosException {
        this.codigo = Validaciones.requerido(codigo, "código");
        actualizarDatos(nombre, especialidad, disponibilidad);
    }

    public final void actualizarDatos(String nombre, String especialidad, Disponibilidad disponibilidad)
            throws DatosInvalidosException {
        String n = Validaciones.longitudMaxima(Validaciones.requerido(nombre, "nombre"), 100, "nombre");
        String e = Validaciones.longitudMaxima(Validaciones.requerido(especialidad, "especialidad"), 100, "especialidad");
        if (disponibilidad == null) {
            throw new DatosInvalidosException("La disponibilidad es obligatoria.");
        }
        this.nombre = n;
        this.especialidad = e;
        this.disponibilidad = disponibilidad;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public Disponibilidad getDisponibilidad() { return disponibilidad; }
    public EstadoRegistro getEstado() { return estado; }
    public void setEstado(EstadoRegistro estado) { this.estado = estado; }
    public boolean isActivo() { return estado == EstadoRegistro.ACTIVO; }
}
