package com.citas.controller;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Cita;
import com.citas.model.EstadoCita;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import com.citas.service.CitaService;
import com.citas.service.PoliticaAcceso;
import com.citas.util.Entrada;
import com.citas.util.Flash;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Agenda de citas: programar, confirmar, cancelar y atender. La logica esta en CitaService. */
@WebServlet("/citas")
public class CitaServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        CitaService citas = ctx().citas();

        if ("nueva".equals(req.getParameter("accion"))) {
            PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR, Rol.PACIENTE);
            Map<String, String> porDefecto = new HashMap<>();
            String preseleccion = req.getParameter("profesional");
            if (preseleccion != null) {
                porDefecto.put("codigoProfesional", preseleccion);
            }
            if (usuario.isPaciente()) {
                req.setAttribute("pacienteActual", ctx().pacientes().obtener(usuario.getCodigoAsociado()));
            } else {
                req.setAttribute("pacientes", ctx().pacientes().listarActivos());
            }
            req.setAttribute("profesionales", ctx().profesionales().listarActivos());
            req.setAttribute("hoy", citas.hoy());
            req.setAttribute("form", formulario(req, porDefecto));
            vista(req, resp, "cita-form", "Nueva cita");
            return;
        }

        List<Cita> lista;
        if (usuario.isAdmin()) {
            lista = citas.listar();
        } else if (usuario.isProfesional()) {
            lista = citas.listarPorProfesional(usuario.getCodigoAsociado());
        } else {
            lista = citas.listarPorPaciente(usuario.getCodigoAsociado());
        }

        EstadoCita filtroEstado = null;
        String estadoTexto = req.getParameter("estado");
        if (estadoTexto != null && !estadoTexto.isEmpty()) {
            try {
                filtroEstado = EstadoCita.valueOf(estadoTexto);
            } catch (IllegalArgumentException e) {
                filtroEstado = null; // filtro invalido: se ignora
            }
        }
        LocalDate filtroFecha = null;
        try {
            filtroFecha = Entrada.fechaOpcional(req.getParameter("fecha"));
        } catch (DatosInvalidosException e) {
            filtroFecha = null;
        }
        final EstadoCita fe = filtroEstado;
        final LocalDate ff = filtroFecha;
        lista = lista.stream()
                .filter(c -> fe == null || c.getEstado() == fe)
                .filter(c -> ff == null || c.getFecha().equals(ff))
                .collect(Collectors.toList());

        req.setAttribute("citas", lista);
        req.setAttribute("estados", EstadoCita.values());
        req.setAttribute("filtroEstado", fe == null ? "" : fe.name());
        req.setAttribute("filtroFecha", ff == null ? "" : ff.toString());
        vista(req, resp, "citas", "Citas");
    }

    @Override
    protected void procesar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        CitaService citas = ctx().citas();
        String destino = "/citas";
        switch (accionRequerida(req)) {
            case "crear": {
                PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR, Rol.PACIENTE);
                String codigoPaciente = usuario.isPaciente() ? usuario.getCodigoAsociado() : req.getParameter("codigoPaciente");
                Cita c = citas.programarCita(codigoPaciente, req.getParameter("codigoProfesional"),
                        Entrada.fecha(req.getParameter("fecha")), Entrada.hora(req.getParameter("hora")),
                        req.getParameter("motivo"));
                Flash.ok(req, "Cita " + c.getCodigo() + " programada para el " + c.getFecha() + " a las " + c.getHora() + ".");
                break;
            }
            case "confirmar": {
                Cita c = citas.obtener(req.getParameter("codigo"));
                PoliticaAcceso.exigirConfirmacion(usuario, c);
                citas.confirmarCita(c.getCodigo());
                Flash.ok(req, "Cita " + c.getCodigo() + " confirmada.");
                break;
            }
            case "cancelar": {
                Cita c = citas.obtener(req.getParameter("codigo"));
                PoliticaAcceso.exigirAccesoACita(usuario, c);
                citas.cancelarCita(c.getCodigo());
                Flash.ok(req, "Cita " + c.getCodigo() + " cancelada.");
                break;
            }
            case "atender": {
                Cita c = citas.obtener(req.getParameter("codigo"));
                PoliticaAcceso.exigirAtencion(usuario, c);
                citas.atenderCita(c.getCodigo());
                Flash.ok(req, "Cita " + c.getCodigo() + " registrada como atendida. Ya puede registrar su seguimiento.");
                break;
            }
            case "recordatorios": {
                PoliticaAcceso.exigirRol(usuario, Rol.ADMINISTRADOR);
                LocalDate manana = citas.hoy().plusDays(1);
                int n = ctx().recordatorios().enviarRecordatorios(manana);
                Flash.ok(req, "Recordatorios enviados para las citas del " + manana + ": " + n + ".");
                destino = "/dashboard";
                break;
            }
            default:
                throw new DatosInvalidosException("Acción no válida.");
        }
        redirigir(req, resp, destino);
    }

    @Override
    protected String rutaRetorno(HttpServletRequest req) {
        String accion = req.getParameter("accion");
        if ("crear".equals(accion)) {
            return "/citas?accion=nueva";
        }
        return "recordatorios".equals(accion) ? "/dashboard" : "/citas";
    }
}
