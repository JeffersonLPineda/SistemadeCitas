package com.citas.controller;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Cita;
import com.citas.model.Seguimiento;
import com.citas.model.Usuario;
import com.citas.service.CitaService;
import com.citas.service.PoliticaAcceso;
import com.citas.util.Flash;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Seguimiento posterior a la atencion: observaciones, recomendaciones y proxima accion. */
@WebServlet("/seguimientos")
public class SeguimientoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        CitaService citas = ctx().citas();
        String codigo = req.getParameter("cita");
        List<Seguimiento> lista;
        Cita cita = null;
        boolean puedeRegistrar = false;

        if (codigo != null && !codigo.isEmpty()) {
            cita = citas.obtener(codigo);
            PoliticaAcceso.exigirAccesoACita(usuario, cita);
            lista = ctx().seguimientos().listarPorCita(cita.getCodigo());
            puedeRegistrar = PoliticaAcceso.esProfesionalDe(usuario, cita) && cita.isAtendida();
        } else {
            lista = ctx().seguimientos().listar().stream()
                    .filter(s -> PoliticaAcceso.puedeVerCita(usuario, s.getCita()))
                    .collect(Collectors.toList());
        }

        // Citas atendidas del usuario (acceso rapido para registrar seguimiento)
        List<Cita> atendidas = usuario.isPaciente() ? Collections.<Cita>emptyList()
                : citas.listar().stream()
                        .filter(c -> c.isAtendida() && PoliticaAcceso.puedeVerCita(usuario, c))
                        .collect(Collectors.toList());

        req.setAttribute("seguimientos", lista);
        req.setAttribute("cita", cita);
        req.setAttribute("puedeRegistrar", puedeRegistrar);
        req.setAttribute("atendidas", atendidas);
        req.setAttribute("form", formulario(req, null));
        vista(req, resp, "seguimiento", "Seguimiento");
    }

    @Override
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        if (!"registrar".equals(accionRequerida(req))) {
            throw new DatosInvalidosException("Acción no válida.");
        }
        Cita cita = ctx().citas().obtener(req.getParameter("cita"));
        PoliticaAcceso.exigirAtencion(usuario, cita);
        Seguimiento s = ctx().seguimientos().registrar(cita.getCodigo(), req.getParameter("observaciones"),
                req.getParameter("recomendaciones"), req.getParameter("proximaAccion"));
        Flash.ok(req, "Seguimiento " + s.getCodigo() + " registrado para la cita " + cita.getCodigo() + ".");
        redirigir(req, resp, "/seguimientos?cita=" + codificar(cita.getCodigo()));
    }

    @Override
    protected String rutaRetorno(HttpServletRequest req) {
        String cita = req.getParameter("cita");
        return cita == null || cita.isEmpty() ? "/seguimientos" : "/seguimientos?cita=" + codificar(cita);
    }
}
