package com.citas.util;

import com.citas.exception.DatosInvalidosException;
import java.util.regex.Pattern;

/** Validaciones de texto reutilizables por las entidades y los servicios. */
public final class Validaciones {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+\\-() ]{7,20}$");

    private Validaciones() {
    }

    /** Devuelve el texto sin espacios laterales; falla si es nulo o vacio. */
    public static String requerido(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    public static String correo(String valor) throws DatosInvalidosException {
        String c = requerido(valor, "correo").toLowerCase();
        if (!CORREO.matcher(c).matches()) {
            throw new DatosInvalidosException("El correo electrónico no tiene un formato válido.");
        }
        return c;
    }

    /** Telefono opcional: vacio es valido; si se escribe debe tener formato razonable. */
    public static String telefonoOpcional(String valor) throws DatosInvalidosException {
        String t = opcional(valor);
        if (!t.isEmpty() && !TELEFONO.matcher(t).matches()) {
            throw new DatosInvalidosException("El teléfono solo puede contener números, espacios, +, - y paréntesis (7 a 20 caracteres).");
        }
        return t;
    }

    public static String opcional(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public static String longitudMaxima(String valor, int max, String campo) throws DatosInvalidosException {
        if (valor != null && valor.length() > max) {
            throw new DatosInvalidosException("El campo '" + campo + "' no puede superar " + max + " caracteres.");
        }
        return valor;
    }
}
