package com.citas.model;

import com.citas.exception.DatosInvalidosException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Objeto-valor inmutable: dias de la semana y rango horario en que atiende un profesional.
 * Invariantes: al menos un dia; inicio estrictamente menor que fin.
 */
public final class Disponibilidad {

    /** Las citas se programan en bloques de este numero de minutos. */
    public static final int INTERVALO_MINUTOS = 30;

    private final Set<DayOfWeek> dias;
    private final LocalTime inicio;
    private final LocalTime fin;

    public Disponibilidad(Set<DayOfWeek> dias, LocalTime inicio, LocalTime fin) throws DatosInvalidosException {
        if (dias == null || dias.isEmpty()) {
            throw new DatosInvalidosException("Seleccione al menos un día de atención.");
        }
        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new DatosInvalidosException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        this.dias = Collections.unmodifiableSet(EnumSet.copyOf(dias));
        this.inicio = inicio;
        this.fin = fin;
    }

    /** Verdadero si el profesional atiende ese dia de la semana y la hora cae dentro del rango [inicio, fin). */
    public boolean permite(LocalDate fecha, LocalTime hora) {
        return dias.contains(fecha.getDayOfWeek()) && !hora.isBefore(inicio) && hora.isBefore(fin);
    }

    public Set<DayOfWeek> getDias() { return dias; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFin() { return fin; }

    /** Util para las vistas: {@code incluyeDia('MONDAY')}. */
    public boolean incluyeDia(String nombreDia) {
        return dias.stream().anyMatch(d -> d.name().equals(nombreDia));
    }

    public String getDiasTexto() {
        return dias.stream().map(Disponibilidad::abreviatura).collect(Collectors.joining(", "));
    }

    public String getResumen() {
        return getDiasTexto() + " de " + inicio + " a " + fin;
    }

    private static String abreviatura(DayOfWeek d) {
        switch (d) {
            case MONDAY: return "Lun";
            case TUESDAY: return "Mar";
            case WEDNESDAY: return "Mié";
            case THURSDAY: return "Jue";
            case FRIDAY: return "Vie";
            case SATURDAY: return "Sáb";
            default: return "Dom";
        }
    }
}
