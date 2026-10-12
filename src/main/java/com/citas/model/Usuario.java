package com.citas.model;

import com.citas.exception.DatosInvalidosException;
import com.citas.util.Validaciones;

/**
 * Usuario del sistema (cuenta de acceso).
 * Invariantes: nombre, correo y rol no nulos; la contrasena se guarda solo como hash;
 * los roles PROFESIONAL y PACIENTE estan asociados a un codigo de profesional/paciente.
 */
public class Usuario {

    private int id;
    private String nombre;
    private final String correo;
    private String passwordHash;
    private final Rol rol;
    private EstadoRegistro estado = EstadoRegistro.ACTIVO;
    private final String codigoAsociado;

    public Usuario(int id, String nombre, String correo, String passwordHash, Rol rol, String codigoAsociado)
            throws DatosInvalidosException {
        if (rol == null) {
            throw new DatosInvalidosException("El rol es obligatorio.");
        }
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new DatosInvalidosException("La contraseña es obligatoria.");
        }
        this.id = id;
        this.nombre = Validaciones.requerido(nombre, "nombre");
        this.correo = Validaciones.correo(correo);
        this.passwordHash = passwordHash;
        this.rol = rol;
        if (rol != Rol.ADMINISTRADOR && (codigoAsociado == null || codigoAsociado.trim().isEmpty())) {
            throw new DatosInvalidosException("Un usuario " + rol.getEtiqueta() + " debe estar asociado a un registro.");
        }
        this.codigoAsociado = rol == Rol.ADMINISTRADOR ? null : codigoAsociado.trim();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public EstadoRegistro getEstado() { return estado; }
    public void setEstado(EstadoRegistro estado) { this.estado = estado; }
    public String getCodigoAsociado() { return codigoAsociado; }

    public boolean isActivo() { return estado == EstadoRegistro.ACTIVO; }
    public boolean isAdmin() { return rol == Rol.ADMINISTRADOR; }
    public boolean isProfesional() { return rol == Rol.PROFESIONAL; }
    public boolean isPaciente() { return rol == Rol.PACIENTE; }
}
