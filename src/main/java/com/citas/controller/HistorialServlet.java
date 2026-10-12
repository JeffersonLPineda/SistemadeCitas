package com.citas.controller;

import com.citas.model.Cita;
import com.citas.model.Paciente;
import com.citas.model.Usuario;
import com.citas.service.PoliticaAcceso;
import com.citas.tda.HistorialPaciente;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Historial cronologico de citas y seguimientos de un paciente. */
@WebServlet("/historial")
public class HistorialServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario) throws Exception {
        String codigoPaciente = req.getParameter("paciente");
        List<Cita> citasProfesional = usuario.isProfesional()
                ? ctx().citas().listarPorProfesional(usuario.getCodigoAsociado())
                : Collections.<Cita>emptyList();

        if (usuario.isPaciente()) {
            codigoPaciente = usuario.getCodigoAsociado();
        } else if (usuario.isAdmin()) {
            req.setAttribute("opciones", ctx().pacientes().listar());
        } else {
            Map<String, Paciente> propios = new LinkedHashMap<>();
            for (Cita c : citasProfesional) {
                propios.putIfAbsent(c.getPaciente().getCodigo(), c.getPaciente());
            }
            req.setAttribute("opciones", new ArrayList<>(propios.values()));
        }

        if (codigoPaciente != null && !codigoPaciente.isEmpty()) {
            PoliticaAcceso.exigirAccesoAHistorial(usuario, codigoPaciente, citasProfesional);
            HistorialPaciente historial = ctx().historial().delPaciente(codigoPaciente);
            req.setAttribute("historial", historial);
            req.setAttribute("eventos", historial.getEventos());
            req.setAttribute("seleccionado", codigoPaciente);
        }
        vista(req, resp, "historial", "Historial");
    }
}
