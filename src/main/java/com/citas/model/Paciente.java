package com.citas.model;

import com.citas.exception.DatosInvalidosException;
import com.citas.util.Validaciones;

/** Paciente/Cliente. Invariantes: codigo, nombre y correo validos y no vacios; estado no nulo. */
public class Paciente {

    private final String codigo;
    private String nombre;
    private String telefono;
    private String correo;
    private String direccion;
    private EstadoRegistro estado = EstadoRegistro.ACTIVO;

    public Paciente(String codigo, String nombre, String telefono, String correo, String direccion)
            throws DatosInvalidosException {
        this.codigo = Validaciones.requerido(codigo, "código");
        actualizarDatos(nombre, telefono, correo, direccion);
    }

    /**
     * Actualiza los datos de contacto.
     * Precondicion: nombre y correo no vacios, correo con formato valido.
     * Postcondicion: los campos quedan actualizados (o no cambia nada si se lanza la excepcion).
     */
    public final void actualizarDatos(String nombre, String telefono, String correo, String direccion)
            throws DatosInvalidosException {
        String n = Validaciones.longitudMaxima(Validaciones.requerido(nombre, "nombre"), 100, "nombre");
        String c = Validaciones.correo(correo);
        String t = Validaciones.telefonoOpcional(telefono);
        String d = Validaciones.longitudMaxima(Validaciones.opcional(direccion), 200, "dirección");
        this.nombre = n;
        this.correo = c;
        this.telefono = t;
        this.direccion = d;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
    public EstadoRegistro getEstado() { return estado; }
    public void setEstado(EstadoRegistro estado) { this.estado = estado; }
    public boolean isActivo() { return estado == EstadoRegistro.ACTIVO; }
}
