package com.citas.controller;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Paciente;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import com.citas.service.PacienteService;
import com.citas.service.PoliticaAcceso;
import com.citas.util.Flash;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Pacientes/clientes: el administrador registra y actualiza; el profesional solo consulta. */
@WebServlet("/pacientes")
public class PacienteServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR, Rol.PROFESIONAL);
        PacienteService servicio = ctx().pacientes();
        String accion = req.getParameter("accion");

        boolean mostrarForm = false;
        Paciente editar = null;
        Map<String, String> porDefecto = new HashMap<>();
        if (usuario.isAdmin() && "nuevo".equals(accion)) {
            mostrarForm = true;
        } else if (usuario.isAdmin() && "editar".equals(accion)) {
            editar = servicio.obtener(req.getParameter("codigo"));
            mostrarForm = true;
            porDefecto.put("nombre", editar.getNombre());
            porDefecto.put("telefono", editar.getTelefono());
            porDefecto.put("correo", editar.getCorreo());
            porDefecto.put("direccion", editar.getDireccion());
        }

        req.setAttribute("pacientes", servicio.listar());
        req.setAttribute("mostrarForm", mostrarForm);
        req.setAttribute("paciente", editar);
        req.setAttribute("form", formulario(req, porDefecto));
        vista(req, resp, "pacientes", "Pacientes");
    }

    @Override
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR);
        PacienteService servicio = ctx().pacientes();
        switch (accionRequerida(req)) {
            case "crear": {
                String password = req.getParameter("password");
                Paciente p = servicio.registrar(req.getParameter("nombre"), req.getParameter("telefono"),
                        req.getParameter("correo"), req.getParameter("direccion"), password);
                Flash.ok(req, "Paciente " + p.getCodigo() + " registrado"
                        + (password != null && !password.isEmpty() ? " con acceso al sistema." : "."));
                break;
            }
            case "actualizar": {
                Paciente p = servicio.actualizar(req.getParameter("codigo"), req.getParameter("nombre"),
                        req.getParameter("telefono"), req.getParameter("correo"), req.getParameter("direccion"));
                Flash.ok(req, "Datos del paciente " + p.getCodigo() + " actualizados.");
                break;
            }
            case "estado": {
                servicio.cambiarEstado(req.getParameter("codigo"), estadoRegistro(req.getParameter("estado")));
                Flash.ok(req, "Estado del paciente actualizado.");
                break;
            }
            default:
                throw new DatosInvalidosException("Acción no válida.");
        }
        redirigir(req, resp, "/pacientes");
    }

    @Override
    protected String rutaRetorno(HttpServletRequest req) {
        String accion = req.getParameter("accion");
        if ("crear".equals(accion)) {
            return "/pacientes?accion=nuevo";
        }
        if ("actualizar".equals(accion)) {
            return "/pacientes?accion=editar&codigo=" + codificar(req.getParameter("codigo"));
        }
        return "/pacientes";
    }
}
