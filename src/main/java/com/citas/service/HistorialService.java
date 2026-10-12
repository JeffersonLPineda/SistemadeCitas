package com.citas.service;

import com.citas.dao.CitaDao;
import com.citas.dao.SeguimientoDao;
import com.citas.exception.DatosInvalidosException;
import com.citas.model.Cita;
import com.citas.model.Paciente;
import com.citas.model.Seguimiento;
import com.citas.tda.HistorialPaciente;

public class HistorialService {

    private final PacienteService pacienteService;
    private final CitaDao citaDao;
    private final SeguimientoDao seguimientoDao;

    public HistorialService(PacienteService pacienteService, CitaDao citaDao, SeguimientoDao seguimientoDao) {
        this.pacienteService = pacienteService;
        this.citaDao = citaDao;
        this.seguimientoDao = seguimientoDao;
    }

    /** Construye el historial cronologico (TDA) de citas y seguimientos del paciente. */
    public HistorialPaciente delPaciente(String codigoPaciente) throws DatosInvalidosException {
        Paciente paciente = pacienteService.obtener(codigoPaciente);
        HistorialPaciente historial = new HistorialPaciente(paciente);
        for (Cita c : citaDao.listarPorPaciente(codigoPaciente)) {
            historial.agregarCita(c);
        }
        for (Seguimiento s : seguimientoDao.listarPorPaciente(codigoPaciente)) {
            historial.agregarSeguimiento(s);
        }
        return historial;
    }
}
