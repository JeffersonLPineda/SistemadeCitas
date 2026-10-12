package com.citas.util;

import com.citas.exception.DatosInvalidosException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Set;

/** Conversion segura de parametros HTTP (texto) a tipos del dominio. */
public final class Entrada {

    private Entrada() {
    }

    public static LocalDate fecha(String texto) throws DatosInvalidosException {
        try {
            return LocalDate.parse(Validaciones.requerido(texto, "fecha"));
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("La fecha no tiene un formato válido (aaaa-mm-dd).");
        }
    }

    public static LocalDate fechaOpcional(String texto) throws DatosInvalidosException {
        return (texto == null || texto.trim().isEmpty()) ? null : fecha(texto);
    }

    public static LocalTime hora(String texto) throws DatosInvalidosException {
        try {
            return LocalTime.parse(Validaciones.requerido(texto, "hora"));
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("La hora no tiene un formato válido (hh:mm).");
        }
    }

    public static Set<DayOfWeek> dias(String[] valores) throws DatosInvalidosException {
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        if (valores != null) {
            for (String v : valores) {
                try {
                    dias.add(DayOfWeek.valueOf(v));
                } catch (IllegalArgumentException e) {
                    throw new DatosInvalidosException("Día de la semana no válido.");
                }
            }
        }
        return dias;
    }

    public static int entero(String texto, String campo) throws DatosInvalidosException {
        try {
            return Integer.parseInt(Validaciones.requerido(texto, campo));
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El campo '" + campo + "' debe ser un número.");
        }
    }
}
