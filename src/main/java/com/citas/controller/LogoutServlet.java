package com.citas.controller;

import com.citas.model.Usuario;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Cierre de sesion: destruye la sesion con session.invalidate(). */
@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws IOException {
        redirigir(req, resp, "/dashboard"); // cerrar sesion solo se hace por POST (con token CSRF)
    }

    @Override
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        redirigir(req, resp, "/login?salio=1");
    }
}
