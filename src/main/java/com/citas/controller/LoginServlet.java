package com.citas.controller;

import com.citas.config.Contexto;
import com.citas.exception.AutenticacionException;
import com.citas.model.Usuario;
import com.citas.util.Flash;
import com.citas.util.PasswordUtil;
import java.io.IOException;
import java.security.SecureRandom;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Inicio de sesion: autentica, crea una sesion nueva y guarda usuario, rol y token CSRF. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MINUTOS_INACTIVIDAD = 30;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null && sesion.getAttribute("usuario") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.setAttribute("titulo", "Iniciar sesión");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String correo = req.getParameter("correo");
        String password = req.getParameter("password");
        try {
            Usuario usuario = Contexto.instancia().autenticacion().autenticar(correo, password == null ? "" : password);

            HttpSession anterior = req.getSession(false);
            if (anterior != null) {
                anterior.invalidate(); // nueva sesion al iniciar: evita fijacion de sesion
            }
            HttpSession sesion = req.getSession(true);
            sesion.setAttribute("usuario", usuario);
            sesion.setAttribute("rol", usuario.getRol());
            sesion.setAttribute("csrf", tokenAleatorio());
            sesion.setMaxInactiveInterval(MINUTOS_INACTIVIDAD * 60);
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (AutenticacionException e) {
            Flash.errorConDatos(req, e.getMessage()); // conserva el correo escrito (nunca la contraseña)
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private static String tokenAleatorio() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return PasswordUtil.aHex(bytes);
    }
}
