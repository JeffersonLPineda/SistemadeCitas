package com.citas.controller;

import com.citas.model.Cita;
import com.citas.model.EstadoCita;
import com.citas.model.Usuario;
import com.citas.service.CitaService;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Panel principal: indicadores y accesos segun el rol. */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        CitaService citas = ctx().citas();
        LocalDate hoy = citas.hoy();
        Map<String, Object> indicadores = new LinkedHashMap<>();
        List<Cita> propias;

        if (usuario.isAdmin()) {
            propias = citas.listar();
            indicadores.put("Pacientes activos", ctx().pacientes().listarActivos().size());
            indicadores.put("Profesionales activos", ctx().profesionales().listarActivos().size());
            indicadores.put("Usuarios registrados", ctx().usuarios().listar().size());
            req.setAttribute("bitacora", ctx().bitacora().ultimas(5));
        } else if (usuario.isProfesional()) {
            propias = citas.listarPorProfesional(usuario.getCodigoAsociado());
        } else {
            propias = citas.listarPorPaciente(usuario.getCodigoAsociado());
        }

        Map<EstadoCita, Long> conteo = citas.contarPorEstado(propias);
        indicadores.put("Citas de hoy", propias.stream().filter(c -> c.ocupaHorario() && c.getFecha().equals(hoy)).count());
        indicadores.put("Programadas", conteo.get(EstadoCita.PROGRAMADA));
        indicadores.put("Confirmadas", conteo.get(EstadoCita.CONFIRMADA));
        indicadores.put("Atendidas", conteo.get(EstadoCita.ATENDIDA));
        indicadores.put("Canceladas", conteo.get(EstadoCita.CANCELADA));

        List<Cita> proximas = propias.stream()
                .filter(c -> (c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.CONFIRMADA)
                        && !c.getFecha().isBefore(hoy))
                .limit(5)
                .collect(Collectors.toList());

        req.setAttribute("indicadores", indicadores);
        req.setAttribute("proximas", proximas);
        vista(req, resp, "dashboard", "Panel principal");
    }
}
