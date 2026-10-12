package com.citas.service;

import com.citas.dao.CitaDao;
import com.citas.dao.PacienteDao;
import com.citas.dao.ProfesionalDao;
import com.citas.exception.CitaNoEncontradaException;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.EstadoCitaInvalidoException;
import com.citas.exception.FechaInvalidaException;
import com.citas.exception.GestionCitasException;
import com.citas.exception.HorarioNoDisponibleException;
import com.citas.model.Cita;
import com.citas.model.Disponibilidad;
import com.citas.model.EstadoCita;
import com.citas.model.Paciente;
import com.citas.model.Profesional;
import com.citas.notification.Notificador;
import com.citas.tda.Agenda;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Reglas de negocio de la agenda de citas. Los servlets solo delegan en esta clase. */
public class CitaService {

    private final CitaDao citaDao;
    private final PacienteDao pacienteDao;
    private final ProfesionalDao profesionalDao;
    private final Notificador notificador;
    private final Clock reloj;

    public CitaService(CitaDao citaDao, PacienteDao pacienteDao, ProfesionalDao profesionalDao,
                       Notificador notificador, Clock reloj) {
        this.citaDao = citaDao;
        this.pacienteDao = pacienteDao;
        this.profesionalDao = profesionalDao;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    /**
     * Programa una cita.
     * <ul>
     * <li><b>Precondiciones:</b> paciente != null y activo; profesional != null y activo; fecha valida
     * (no anterior a hoy ni hora ya pasada hoy); hora dentro de la disponibilidad y en bloques de 30 min;
     * horario libre para el profesional; motivo no vacio.</li>
     * <li><b>Postcondicion:</b> se registra la cita con codigo unico y estado PROGRAMADA.</li>
     * <li><b>Invariantes:</b> la cita mantiene paciente, profesional y un estado valido;
     * el profesional no tiene dos citas vigentes en la misma fecha y hora.</li>
     * <li><b>Excepciones:</b> FechaInvalidaException, HorarioNoDisponibleException, DatosInvalidosException.</li>
     * </ul>
     */
    public synchronized Cita programarCita(String codigoPaciente, String codigoProfesional, LocalDate fecha,
                                           LocalTime hora, String motivo) throws GestionCitasException {
        Paciente paciente = pacienteDao.buscarPorCodigo(codigoPaciente)
                .orElseThrow(() -> new DatosInvalidosException("El paciente indicado no existe."));
        Profesional profesional = profesionalDao.buscarPorCodigo(codigoProfesional)
                .orElseThrow(() -> new DatosInvalidosException("El profesional indicado no existe."));
        if (!paciente.isActivo()) {
            throw new DatosInvalidosException("El paciente está inactivo y no puede solicitar citas.");
        }
        if (!profesional.isActivo()) {
            throw new DatosInvalidosException("El profesional está inactivo y no recibe citas.");
        }
        if (fecha == null || hora == null) {
            throw new DatosInvalidosException("La fecha y la hora son obligatorias.");
        }
        validarFechaHora(fecha, hora);
        if (!profesional.getDisponibilidad().permite(fecha, hora)) {
            throw new HorarioNoDisponibleException("El profesional no atiende en ese día u hora. Disponibilidad: "
                    + profesional.getDisponibilidad().getResumen() + ".");
        }
        if (hora.getMinute() % Disponibilidad.INTERVALO_MINUTOS != 0 || hora.getSecond() != 0) {
            throw new HorarioNoDisponibleException("Las citas se programan en bloques de "
                    + Disponibilidad.INTERVALO_MINUTOS + " minutos (por ejemplo 09:00 o 09:30).");
        }

        Cita cita = new Cita(citaDao.siguienteCodigo(), paciente, profesional, fecha, hora, motivo);
        Agenda agenda = agendaDe(profesional.getCodigo());
        agenda.insertar(cita); // lanza HorarioNoDisponibleException si el horario ya esta ocupado
        citaDao.guardar(cita);

        notificador.avisar(paciente, "Su cita " + cita.getCodigo() + " con " + profesional.getNombre()
                + " fue programada para el " + fecha + " a las " + hora + ".");
        return cita;
    }

    private void validarFechaHora(LocalDate fecha, LocalTime hora) throws FechaInvalidaException {
        LocalDate hoy = LocalDate.now(reloj);
        if (fecha.isBefore(hoy)) {
            throw new FechaInvalidaException("No se pueden programar citas en fechas anteriores a hoy (" + hoy + ").");
        }
        if (fecha.equals(hoy) && hora.isBefore(LocalTime.now(reloj))) {
            throw new FechaInvalidaException("La hora seleccionada ya pasó.");
        }
    }

    /** Precondicion: cita PROGRAMADA. Postcondicion: CONFIRMADA. */
    public synchronized Cita confirmarCita(String codigo) throws GestionCitasException {
        Cita cita = obtener(codigo);
        cita.confirmar();
        citaDao.actualizar(cita);
        notificador.avisar(cita.getPaciente(), "Su cita " + cita.getCodigo() + " del " + cita.getFecha()
                + " a las " + cita.getHora() + " fue CONFIRMADA.");
        return cita;
    }

    /**
     * Cancela una cita.
     * <b>Precondicion:</b> estado PROGRAMADA o CONFIRMADA. <b>Postcondicion:</b> estado CANCELADA y el horario
     * del profesional queda libre. <b>Excepciones:</b> CitaNoEncontradaException, EstadoCitaInvalidoException.
     */
    public synchronized Cita cancelarCita(String codigo) throws GestionCitasException {
        Cita cita = obtener(codigo);
        cita.cancelar();
        citaDao.actualizar(cita);
        notificador.avisar(cita.getPaciente(), "Su cita " + cita.getCodigo() + " del " + cita.getFecha()
                + " a las " + cita.getHora() + " fue CANCELADA.");
        return cita;
    }

    /**
     * Registra la atencion de una cita.
     * <b>Precondicion:</b> estado CONFIRMADA (una cita CANCELADA no puede atenderse).
     * <b>Postcondicion:</b> estado ATENDIDA. <b>Excepcion:</b> EstadoCitaInvalidoException.
     */
    public synchronized Cita atenderCita(String codigo) throws GestionCitasException {
        Cita cita = obtener(codigo);
        cita.atender();
        citaDao.actualizar(cita);
        return cita;
    }

    public Cita obtener(String codigo) throws CitaNoEncontradaException {
        return citaDao.buscarPorCodigo(codigo).orElseThrow(() -> new CitaNoEncontradaException(codigo));
    }

    /** Agenda (TDA) con las citas vigentes del profesional. */
    public Agenda agendaDe(String codigoProfesional) throws HorarioNoDisponibleException {
        return Agenda.desde(citaDao.listarPorProfesional(codigoProfesional));
    }

    public List<Cita> listar() {
        return citaDao.listar();
    }

    public List<Cita> listarPorPaciente(String codigoPaciente) {
        return citaDao.listarPorPaciente(codigoPaciente);
    }

    public List<Cita> listarPorProfesional(String codigoProfesional) {
        return citaDao.listarPorProfesional(codigoProfesional);
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    public Map<EstadoCita, Long> contarPorEstado(Collection<Cita> citas) {
        Map<EstadoCita, Long> conteo = new EnumMap<>(EstadoCita.class);
        for (EstadoCita e : EstadoCita.values()) {
            conteo.put(e, 0L);
        }
        for (Cita c : citas) {
            conteo.merge(c.getEstado(), 1L, Long::sum);
        }
        return conteo;
    }
}
