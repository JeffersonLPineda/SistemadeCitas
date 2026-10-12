package com.citas.controller;

import com.citas.config.Contexto;
import com.citas.exception.AccesoDenegadoException;
import com.citas.exception.GestionCitasException;
import com.citas.model.Cita;
import com.citas.model.Profesional;
import com.citas.model.Usuario;
import com.citas.service.PoliticaAcceso;
import com.citas.tda.Agenda;
import com.citas.util.Entrada;
import com.citas.util.Json;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Reto adicional: API REST basica (solo lectura) de la agenda de un profesional.
 * GET /api/agenda?profesional=PRO-0001[&fecha=aaaa-mm-dd]  ->  JSON.
 * Requiere sesion iniciada. Un paciente solo ve los horarios ocupados (sin datos de otros pacientes).
 */
@WebServlet("/api/agenda")
public class AgendaApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            error(resp, HttpServletResponse.SC_UNAUTHORIZED, "Se requiere iniciar sesión");
            return;
        }
        try {
            String codigo = req.getParameter("profesional");
            if (usuario.isProfesional()) {
                if (codigo == null || codigo.isEmpty()) {
                    codigo = usuario.getCodigoAsociado();
                } else if (!codigo.equals(usuario.getCodigoAsociado())) {
                    throw new AccesoDenegadoException("Solo puede consultar su propia agenda.");
                }
            }
            Contexto ctx = Contexto.instancia();
            Profesional profesional = ctx.profesionales().obtener(codigo);
            LocalDate fecha = Entrada.fechaOpcional(req.getParameter("fecha"));
            LocalDate hoy = ctx.citas().hoy();

            Agenda agenda = ctx.citas().agendaDe(profesional.getCodigo());
            List<Cita> citas = fecha == null ? agenda.todas() : agenda.citasDelDia(fecha);

            StringBuilder json = new StringBuilder();
            json.append("{\"profesional\":").append(Json.texto(profesional.getCodigo()))
                .append(",\"nombre\":").append(Json.texto(profesional.getNombre()))
                .append(",\"especialidad\":").append(Json.texto(profesional.getEspecialidad()))
                .append(",\"fecha\":").append(fecha == null ? "null" : Json.texto(fecha.toString()))
                .append(",\"citas\":[");
            boolean primero = true;
            for (Cita c : citas) {
                if (usuario.isPaciente() && c.getFecha().isBefore(hoy)) {
                    continue;
                }
                if (!primero) {
                    json.append(',');
                }
                primero = false;
                json.append("{\"fecha\":").append(Json.texto(c.getFecha().toString()))
                    .append(",\"hora\":").append(Json.texto(c.getHora().toString()));
                if (PoliticaAcceso.puedeVerCita(usuario, c)) {
                    json.append(",\"codigo\":").append(Json.texto(c.getCodigo()))
                        .append(",\"estado\":").append(Json.texto(c.getEstado().name()))
                        .append(",\"paciente\":").append(Json.texto(c.getPaciente().getNombre()));
                } else {
                    json.append(",\"estado\":\"OCUPADO\"");
                }
                json.append('}');
            }
            json.append("]}");
            PrintWriter out = resp.getWriter();
            out.print(json);
        } catch (AccesoDenegadoException e) {
            error(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (GestionCitasException e) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void error(HttpServletResponse resp, int estado, String mensaje) throws IOException {
        resp.setStatus(estado);
        resp.getWriter().print("{\"error\":" + Json.texto(mensaje) + "}");
    }
}
