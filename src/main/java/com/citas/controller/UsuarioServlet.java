package com.citas.controller;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import com.citas.service.PoliticaAcceso;
import com.citas.util.Entrada;
import com.citas.util.Flash;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Administracion de usuarios (solo administrador): activar/desactivar y restablecer contrasena. */
@WebServlet("/usuarios")
public class UsuarioServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR);
        req.setAttribute("usuarios", ctx().usuarios().listar());
        vista(req, resp, "usuarios", "Usuarios");
    }

    @Override
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR);
        int id = Entrada.entero(req.getParameter("id"), "id");
        switch (accionRequerida(req)) {
            case "estado":
                ctx().usuarios().cambiarEstado(id, estadoRegistro(req.getParameter("estado")), usuario.getId());
                Flash.ok(req, "Estado del usuario actualizado.");
                break;
            case "password":
                ctx().usuarios().restablecerPassword(id, req.getParameter("nuevaPassword"));
                Flash.ok(req, "Contraseña restablecida.");
                break;
            default:
                throw new DatosInvalidosException("Acción no válida.");
        }
        redirigir(req, resp, "/usuarios");
    }
}
