package com.citas.model;

import com.citas.exception.DatosInvalidosException;
import com.citas.exception.EstadoCitaInvalidoException;
import com.citas.util.Validaciones;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Cita entre un paciente y un profesional.
 *
 * <p><b>Invariantes de clase:</b> paciente, profesional, fecha, hora y estado nunca son nulos;
 * el codigo no es vacio; el estado solo cambia segun {@link EstadoCita#puedePasarA(EstadoCita)}.</p>
 */
public class Cita {

    private final String codigo;
    private final Paciente paciente;
    private final Profesional profesional;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String motivo;
    private volatile EstadoCita estado;

    /**
     * Crea una cita en estado PROGRAMADA.
     * <p>Precondiciones: codigo, paciente, profesional, fecha, hora y motivo no nulos/vacios.<br>
     * Postcondicion: estado == PROGRAMADA.<br>
     * Excepcion: DatosInvalidosException si alguna precondicion falla.</p>
     * La validacion de fechas pasadas y horarios corresponde al servicio ({@code CitaService}).
     */
    public Cita(String codigo, Paciente paciente, Profesional profesional, LocalDate fecha,
                LocalTime hora, String motivo) throws DatosInvalidosException {
        this.codigo = Validaciones.requerido(codigo, "código");
        if (paciente == null) {
            throw new DatosInvalidosException("Toda cita debe tener un paciente.");
        }
        if (profesional == null) {
            throw new DatosInvalidosException("Toda cita debe tener un profesional.");
        }
        if (fecha == null || hora == null) {
            throw new DatosInvalidosException("La fecha y la hora son obligatorias.");
        }
        this.paciente = paciente;
        this.profesional = profesional;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = Validaciones.longitudMaxima(Validaciones.requerido(motivo, "motivo"), 300, "motivo");
        this.estado = EstadoCita.PROGRAMADA;
    }

    /**
     * Reconstruye una cita ya guardada (uso exclusivo de la capa DAO): conserva el estado persistido.
     * Precondicion: los mismos datos validos que el constructor y un estado no nulo.
     */
    public static Cita restaurar(String codigo, Paciente paciente, Profesional profesional, LocalDate fecha,
                                 LocalTime hora, String motivo, EstadoCita estado) throws DatosInvalidosException {
        if (estado == null) {
            throw new DatosInvalidosException("El estado de la cita es obligatorio.");
        }
        Cita cita = new Cita(codigo, paciente, profesional, fecha, hora, motivo);
        cita.estado = estado;
        return cita;
    }

    /**
     * Precondicion: estado == PROGRAMADA. Postcondicion: estado == CONFIRMADA.
     * @throws EstadoCitaInvalidoException si la cita no esta PROGRAMADA.
     */
    public void confirmar() throws EstadoCitaInvalidoException {
        cambiarEstado(EstadoCita.CONFIRMADA, "Solo se puede confirmar una cita PROGRAMADA (estado actual: %s).");
    }

    /**
     * Precondicion: estado PROGRAMADA o CONFIRMADA. Postcondicion: estado == CANCELADA.
     * @throws EstadoCitaInvalidoException si la cita ya fue atendida o cancelada.
     */
    public void cancelar() throws EstadoCitaInvalidoException {
        cambiarEstado(EstadoCita.CANCELADA, "Solo se puede cancelar una cita PROGRAMADA o CONFIRMADA (estado actual: %s).");
    }

    /**
     * Precondicion: estado == CONFIRMADA (una cita CANCELADA nunca puede atenderse).
     * Postcondicion: estado == ATENDIDA.
     * @throws EstadoCitaInvalidoException si la cita no esta CONFIRMADA.
     */
    public void atender() throws EstadoCitaInvalidoException {
        cambiarEstado(EstadoCita.ATENDIDA, "Solo se puede atender una cita CONFIRMADA (estado actual: %s).");
    }

    private void cambiarEstado(EstadoCita destino, String plantillaError) throws EstadoCitaInvalidoException {
        if (!estado.puedePasarA(destino)) {
            throw new EstadoCitaInvalidoException(String.format(plantillaError, estado.getEtiqueta().toUpperCase()));
        }
        estado = destino;
    }

    /** Una cita cancelada libera el horario del profesional. */
    public boolean ocupaHorario() {
        return estado != EstadoCita.CANCELADA;
    }

    public String getCodigo() { return codigo; }
    public Paciente getPaciente() { return paciente; }
    public Profesional getProfesional() { return profesional; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getMotivo() { return motivo; }
    public EstadoCita getEstado() { return estado; }
    public LocalDateTime getFechaHora() { return LocalDateTime.of(fecha, hora); }

    // Ayudas para las vistas JSP
    public boolean isConfirmable() { return estado == EstadoCita.PROGRAMADA; }
    public boolean isCancelable() { return estado.puedePasarA(EstadoCita.CANCELADA); }
    public boolean isAtendible() { return estado == EstadoCita.CONFIRMADA; }
    public boolean isAtendida() { return estado == EstadoCita.ATENDIDA; }
}
