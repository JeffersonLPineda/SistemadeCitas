package com.citas.filter;

import com.citas.model.Usuario;
import com.citas.util.Flash;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Protege TODAS las pantallas: sin sesion valida (usuario guardado en HttpSession) solo se permite /login.
 * Ademas valida el token anti-CSRF en cada POST y agrega cabeceras de seguridad.
 */
@WebFilter(urlPatterns = "/*")
public class AutenticacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        String ruta = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if (ruta.startsWith("/css/") || ruta.equals("/favicon.ico")) {
            chain.doFilter(req, resp);
            return;
        }

        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Cache-Control", "no-store"); // evita ver pantallas protegidas con el boton "atras" tras cerrar sesion
        Flash.trasladar(req);

        if (ruta.equals("/login")) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario != null && !usuario.isActivo()) { // desactivado por el administrador mientras tenia sesion
            sesion.invalidate();
            usuario = null;
        }
        if (usuario == null) {
            if (ruta.startsWith("/api/")) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Se requiere iniciar sesión");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        if ("POST".equalsIgnoreCase(req.getMethod()) && !tokenValido(req, sesion)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token de seguridad inválido. Recargue la página e intente de nuevo.");
            return;
        }
        chain.doFilter(req, resp);
    }

    private boolean tokenValido(HttpServletRequest req, HttpSession sesion) {
        Object esperado = sesion.getAttribute("csrf");
        String recibido = req.getParameter("csrf");
        return esperado instanceof String && recibido != null
                && MessageDigest.isEqual(((String) esperado).getBytes(StandardCharsets.UTF_8),
                        recibido.getBytes(StandardCharsets.UTF_8));
    }
}
