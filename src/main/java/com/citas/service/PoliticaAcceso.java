package com.citas.service;

import com.citas.exception.AccesoDenegadoException;
import com.citas.model.Cita;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import java.util.Collection;

/** Reglas de autorizacion por rol y por pertenencia de los datos (quien puede ver/operar cada cita). */
public final class PoliticaAcceso {

    private PoliticaAcceso() {
    }

    public static void exigirRol(Usuario u, Rol... permitidos) throws AccesoDenegadoException {
        for (Rol r : permitidos) {
            if (u.getRol() == r) {
                return;
            }
        }
        throw new AccesoDenegadoException("Su rol no tiene permiso para esta acción.");
    }

    public static boolean esProfesionalDe(Usuario u, Cita c) {
        return u.isProfesional() && c.getProfesional().getCodigo().equals(u.getCodigoAsociado());
    }

    public static boolean esPacienteDe(Usuario u, Cita c) {
        return u.isPaciente() && c.getPaciente().getCodigo().equals(u.getCodigoAsociado());
    }

    public static boolean puedeVerCita(Usuario u, Cita c) {
        return u.isAdmin() || esProfesionalDe(u, c) || esPacienteDe(u, c);
    }

    public static void exigirAccesoACita(Usuario u, Cita c) throws AccesoDenegadoException {
        if (!puedeVerCita(u, c)) {
            throw new AccesoDenegadoException("No tiene acceso a esta cita.");
        }
    }

    /** Confirmar: administrador o el profesional de la cita. */
    public static void exigirConfirmacion(Usuario u, Cita c) throws AccesoDenegadoException {
        if (!(u.isAdmin() || esProfesionalDe(u, c))) {
            throw new AccesoDenegadoException("Solo el administrador o el profesional de la cita pueden confirmarla.");
        }
    }

    /** Atender y registrar seguimiento: solo el profesional dueño de la cita. */
    public static void exigirAtencion(Usuario u, Cita c) throws AccesoDenegadoException {
        if (!esProfesionalDe(u, c)) {
            throw new AccesoDenegadoException("Solo el profesional de la cita puede atenderla o registrar su seguimiento.");
        }
    }

    /**
     * Historial de un paciente: administrador; el propio paciente; o un profesional que haya tenido citas con el.
     * @param citasDelProfesional citas del profesional autenticado (solo se usa para el rol PROFESIONAL).
     */
    public static void exigirAccesoAHistorial(Usuario u, String codigoPaciente, Collection<Cita> citasDelProfesional)
            throws AccesoDenegadoException {
        boolean permitido = u.isAdmin()
                || (u.isPaciente() && codigoPaciente.equals(u.getCodigoAsociado()))
                || (u.isProfesional() && citasDelProfesional.stream()
                        .anyMatch(c -> c.getPaciente().getCodigo().equals(codigoPaciente)));
        if (!permitido) {
            throw new AccesoDenegadoException("No tiene acceso al historial de este paciente.");
        }
    }
}
