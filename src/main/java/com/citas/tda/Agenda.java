package com.citas.tda;

import com.citas.exception.HorarioNoDisponibleException;
import com.citas.model.Cita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * TDA Agenda: coleccion ordenada cronologicamente de las citas vigentes de UN profesional.
 *
 * <p><b>Modelo abstracto:</b> funcion parcial (fecha, hora) -> cita.<br>
 * <b>Operaciones:</b> insertar, estaOcupado, obtener, liberar, citasDelDia, todas, tamano, estaVacia.<br>
 * <b>Invariantes:</b> no existen dos citas con la misma fecha y hora; solo contiene citas que ocupan
 * horario (no canceladas); las citas se devuelven siempre ordenadas por fecha y hora.</p>
 */
public class Agenda {

    private final TreeMap<LocalDateTime, Cita> citas = new TreeMap<>();

    /**
     * Construye la agenda a partir de citas existentes; ignora las canceladas.
     * @throws HorarioNoDisponibleException si la coleccion contiene dos citas vigentes en el mismo horario.
     */
    public static Agenda desde(Collection<Cita> citas) throws HorarioNoDisponibleException {
        Agenda agenda = new Agenda();
        for (Cita c : citas) {
            if (c.ocupaHorario()) {
                agenda.insertar(c);
            }
        }
        return agenda;
    }

    /**
     * Precondiciones: cita != null; cita.ocupaHorario(); el horario (fecha+hora) esta libre.
     * Postcondicion: la cita queda en la agenda y tamano() aumenta en 1.
     * @throws HorarioNoDisponibleException si ya existe una cita vigente en esa fecha y hora.
     */
    public void insertar(Cita cita) throws HorarioNoDisponibleException {
        Objects.requireNonNull(cita, "La cita es obligatoria");
        if (!cita.ocupaHorario()) {
            throw new IllegalArgumentException("Una cita cancelada no ocupa horario en la agenda");
        }
        if (citas.containsKey(cita.getFechaHora())) {
            throw new HorarioNoDisponibleException("El profesional ya tiene una cita el "
                    + cita.getFecha() + " a las " + cita.getHora() + ".");
        }
        citas.put(cita.getFechaHora(), cita);
    }

    public boolean estaOcupado(LocalDate fecha, LocalTime hora) {
        return citas.containsKey(LocalDateTime.of(fecha, hora));
    }

    public Optional<Cita> obtener(LocalDate fecha, LocalTime hora) {
        return Optional.ofNullable(citas.get(LocalDateTime.of(fecha, hora)));
    }

    /** Quita la cita de la agenda (p. ej. al cancelarse). Devuelve true si estaba. */
    public boolean liberar(Cita cita) {
        Cita actual = citas.get(cita.getFechaHora());
        if (actual != null && actual.getCodigo().equals(cita.getCodigo())) {
            citas.remove(cita.getFechaHora());
            return true;
        }
        return false;
    }

    public List<Cita> citasDelDia(LocalDate fecha) {
        return new ArrayList<>(citas.subMap(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay()).values());
    }

    public List<Cita> todas() {
        return new ArrayList<>(citas.values());
    }

    public int tamano() {
        return citas.size();
    }

    public boolean estaVacia() {
        return citas.isEmpty();
    }
}
