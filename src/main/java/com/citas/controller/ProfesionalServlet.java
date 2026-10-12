package com.citas.controller;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Profesional;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import com.citas.service.PoliticaAcceso;
import com.citas.service.ProfesionalService;
import com.citas.util.Entrada;
import com.citas.util.Flash;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Profesionales: todos los roles consultan; solo el administrador registra y actualiza. */
@WebServlet("/profesionales")
public class ProfesionalServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        ProfesionalService servicio = ctx().profesionales();
        String accion = req.getParameter("accion");

        boolean mostrarForm = false;
        Profesional editar = null;
        Map<String, String> porDefecto = new HashMap<>();
        if (usuario.isAdmin() && "nuevo".equals(accion)) {
            mostrarForm = true;
            porDefecto.put("dias", "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY");
            porDefecto.put("horaInicio", "08:00");
            porDefecto.put("horaFin", "16:00");
        } else if (usuario.isAdmin() && "editar".equals(accion)) {
            editar = servicio.obtener(req.getParameter("codigo"));
            mostrarForm = true;
            porDefecto.put("nombre", editar.getNombre());
            porDefecto.put("especialidad", editar.getEspecialidad());
            porDefecto.put("dias", editar.getDisponibilidad().getDias().stream()
                    .map(DayOfWeek::name).collect(Collectors.joining(",")));
            porDefecto.put("horaInicio", editar.getDisponibilidad().getInicio().toString());
            porDefecto.put("horaFin", editar.getDisponibilidad().getFin().toString());
        }

        req.setAttribute("profesionales", servicio.listar());
        req.setAttribute("mostrarForm", mostrarForm);
        req.setAttribute("profesional", editar);
        req.setAttribute("form", formulario(req, porDefecto));
        vista(req, resp, "profesionales", "Profesionales");
    }

    @Override
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR);
        ProfesionalService servicio = ctx().profesionales();
        switch (accionRequerida(req)) {
            case "crear": {
                Set<DayOfWeek> dias = Entrada.dias(req.getParameterValues("dias"));
                LocalTime inicio = Entrada.hora(req.getParameter("horaInicio"));
                LocalTime fin = Entrada.hora(req.getParameter("horaFin"));
                String password = req.getParameter("password");
                Profesional p = servicio.registrar(req.getParameter("nombre"), req.getParameter("especialidad"),
                        dias, inicio, fin, req.getParameter("correoAcceso"), password);
                Flash.ok(req, "Profesional " + p.getCodigo() + " registrado"
                        + (password != null && !password.isEmpty() ? " con acceso al sistema." : "."));
                break;
            }
            case "actualizar": {
                Set<DayOfWeek> dias = Entrada.dias(req.getParameterValues("dias"));
                LocalTime inicio = Entrada.hora(req.getParameter("horaInicio"));
                LocalTime fin = Entrada.hora(req.getParameter("horaFin"));
                Profesional p = servicio.actualizar(req.getParameter("codigo"), req.getParameter("nombre"),
                        req.getParameter("especialidad"), dias, inicio, fin);
                Flash.ok(req, "Datos del profesional " + p.getCodigo() + " actualizados.");
                break;
            }
            case "estado": {
                servicio.cambiarEstado(req.getParameter("codigo"), estadoRegistro(req.getParameter("estado")));
                Flash.ok(req, "Estado del profesional actualizado.");
                break;
            }
            default:
                throw new DatosInvalidosException("Acción no válida.");
        }
        redirigir(req, resp, "/profesionales");
    }

    @Override
    protected String rutaRetorno(HttpServletRequest req) {
        String accion = req.getParameter("accion");
        if ("crear".equals(accion)) {
            return "/profesionales?accion=nuevo";
        }
        if ("actualizar".equals(accion)) {
            return "/profesionales?accion=editar&codigo=" + codificar(req.getParameter("codigo"));
        }
        return "/profesionales";
    }
}
