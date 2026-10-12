package com.citas.model;

import com.citas.exception.DatosInvalidosException;
import com.citas.exception.EstadoCitaInvalidoException;
import com.citas.util.Validaciones;
import java.time.LocalDate;

/**
 * Seguimiento posterior a una cita.
 * Invariante: solo existe para citas ATENDIDAS; las observaciones no son vacias.
 */
public class Seguimiento {

    private final String codigo;
    private final Cita cita;
    private final LocalDate fecha;
    private final String observaciones;
    private final String recomendaciones;
    private final String proximaAccion;

    /**
     * Precondiciones: cita != null y cita.estado == ATENDIDA; observaciones no vacias.
     * Postcondicion: seguimiento asociado a la cita.
     * @throws EstadoCitaInvalidoException si la cita aun no fue atendida (o fue cancelada).
     * @throws DatosInvalidosException si faltan datos obligatorios.
     */
    public Seguimiento(String codigo, Cita cita, LocalDate fecha, String observaciones,
                       String recomendaciones, String proximaAccion)
            throws EstadoCitaInvalidoException, DatosInvalidosException {
        if (cita == null) {
            throw new DatosInvalidosException("El seguimiento debe estar relacionado con una cita.");
        }
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw new EstadoCitaInvalidoException(
                    "Solo se puede registrar seguimiento de una cita ATENDIDA (estado actual: "
                    + cita.getEstado().getEtiqueta().toUpperCase() + ").");
        }
        if (fecha == null) {
            throw new DatosInvalidosException("La fecha del seguimiento es obligatoria.");
        }
        this.codigo = Validaciones.requerido(codigo, "código");
        this.cita = cita;
        this.fecha = fecha;
        this.observaciones = Validaciones.longitudMaxima(Validaciones.requerido(observaciones, "observaciones"), 1000, "observaciones");
        this.recomendaciones = Validaciones.longitudMaxima(Validaciones.opcional(recomendaciones), 1000, "recomendaciones");
        this.proximaAccion = Validaciones.longitudMaxima(Validaciones.opcional(proximaAccion), 300, "próxima acción");
    }

    public String getCodigo() { return codigo; }
    public Cita getCita() { return cita; }
    public LocalDate getFecha() { return fecha; }
    public String getObservaciones() { return observaciones; }
    public String getRecomendaciones() { return recomendaciones; }
    public String getProximaAccion() { return proximaAccion; }
}
