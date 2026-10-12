package com.citas.util;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Mensajes "flash" (patron Post-Redirect-Get): se guardan en la sesion y se muestran
 * una sola vez en la siguiente pantalla.
 */
public final class Flash {

    private static final String OK = "flash.ok";
    private static final String ERROR = "flash.error";
    private static final String PREVIO = "flash.previo";

    private Flash() {
    }

    public static void ok(HttpServletRequest req, String mensaje) {
        req.getSession(true).setAttribute(OK, mensaje);
    }

    public static void error(HttpServletRequest req, String mensaje) {
        req.getSession(true).setAttribute(ERROR, mensaje);
    }

    /** Guarda un error y los datos escritos en el formulario (sin contrasenas ni token) para repoblarlo. */
    public static void errorConDatos(HttpServletRequest req, String mensaje) {
        HttpSession s = req.getSession(true);
        s.setAttribute(ERROR, mensaje);
        Map<String, String> previo = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            String nombre = e.getKey();
            if (nombre.toLowerCase().contains("password") || nombre.equals("csrf") || e.getValue().length == 0) {
                continue;
            }
            previo.put(nombre, String.join(",", e.getValue()));
        }
        s.setAttribute(PREVIO, previo);
    }

    /** Pasa los mensajes de la sesion a atributos de la peticion y los elimina de la sesion. */
    public static void trasladar(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return;
        }
        mover(s, req, OK, "mensajeOk");
        mover(s, req, ERROR, "mensajeError");
        mover(s, req, PREVIO, "previo");
    }

    private static void mover(HttpSession s, HttpServletRequest req, String clave, String destino) {
        Object valor = s.getAttribute(clave);
        if (valor != null) {
            req.setAttribute(destino, valor);
            s.removeAttribute(clave);
        }
    }
}
