package com.citas.controller;

import com.citas.config.Contexto;
import com.citas.exception.AccesoDenegadoException;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.GestionCitasException;
import com.citas.model.EstadoRegistro;
import com.citas.model.Usuario;
import com.citas.util.Flash;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Base de los controladores: valida la sesion, delega en {@link #mostrar} (GET) o {@link #procesar} (POST)
 * y traduce las excepciones de negocio a mensajes para el usuario (sin logica de negocio aqui).
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected Contexto ctx() {
        return Contexto.instancia();
    }

    /** Atiende GET. Por defecto el metodo no esta permitido. */
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** Atiende POST. Por defecto el metodo no esta permitido. */
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** Pantalla a la que se vuelve cuando un POST falla (con el mensaje y los datos escritos). */
    protected String rutaRetorno(HttpServletRequest req) {
        return req.getServletPath();
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ejecutar(req, resp, false);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ejecutar(req, resp, true);
    }

    private void ejecutar(HttpServletRequest req, HttpServletResponse resp, boolean post)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) { // defensa adicional: el filtro ya debio redirigir
            redirigir(req, resp, "/login");
            return;
        }
        try {
            if (post) {
                procesar(req, resp, usuario);
            } else {
                mostrar(req, resp, usuario);
            }
        } catch (AccesoDenegadoException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (GestionCitasException e) {
            if (post) {
                Flash.errorConDatos(req, e.getMessage());
                redirigir(req, resp, rutaRetorno(req));
            } else if (req.getServletPath().equals("/dashboard")) {
                throw new ServletException(e);
            } else {
                Flash.error(req, e.getMessage());
                redirigir(req, resp, "/dashboard");
            }
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void vista(HttpServletRequest req, HttpServletResponse resp, String nombre, String titulo)
            throws ServletException, IOException {
        req.setAttribute("titulo", titulo);
        req.getRequestDispatcher("/WEB-INF/views/" + nombre + ".jsp").forward(req, resp);
    }

    protected void redirigir(HttpServletRequest req, HttpServletResponse resp, String ruta) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    /** Valores del formulario: los datos escritos antes de un error, o si no hay, los valores por defecto. */
    @SuppressWarnings("unchecked")
    protected Map<String, String> formulario(HttpServletRequest req, Map<String, String> porDefecto) {
        Object previo = req.getAttribute("previo");
        if (previo instanceof Map && !((Map<String, String>) previo).isEmpty()) {
            return (Map<String, String>) previo;
        }
        return porDefecto == null ? Collections.<String, String>emptyMap() : porDefecto;
    }

    protected static EstadoRegistro estadoRegistro(String texto) throws DatosInvalidosException {
        try {
            return EstadoRegistro.valueOf(texto);
        } catch (RuntimeException e) {
            throw new DatosInvalidosException("Estado no válido.");
        }
    }

    protected static String codificar(String texto) {
        try {
            return URLEncoder.encode(texto == null ? "" : texto, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static String accionRequerida(HttpServletRequest req) throws DatosInvalidosException {
        String accion = req.getParameter("accion");
        if (accion == null || accion.isEmpty()) {
            throw new DatosInvalidosException("Acción no válida.");
        }
        return accion;
    }
}
