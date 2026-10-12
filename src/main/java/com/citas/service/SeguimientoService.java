package com.citas.service;

import com.citas.dao.SeguimientoDao;
import com.citas.exception.GestionCitasException;
import com.citas.model.Cita;
import com.citas.model.Seguimiento;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class SeguimientoService {

    private final SeguimientoDao seguimientoDao;
    private final CitaService citaService;
    private final Clock reloj;

    public SeguimientoService(SeguimientoDao seguimientoDao, CitaService citaService, Clock reloj) {
        this.seguimientoDao = seguimientoDao;
        this.citaService = citaService;
        this.reloj = reloj;
    }

    /**
     * Registra el seguimiento de una cita.
     * <b>Precondiciones:</b> la cita existe y esta ATENDIDA; observaciones no vacias.
     * <b>Postcondicion:</b> seguimiento guardado con la fecha de hoy.
     * <b>Excepciones:</b> CitaNoEncontradaException, EstadoCitaInvalidoException, DatosInvalidosException.
     */
    public Seguimiento registrar(String codigoCita, String observaciones, String recomendaciones, String proximaAccion)
            throws GestionCitasException {
        Cita cita = citaService.obtener(codigoCita);
        Seguimiento s = new Seguimiento(seguimientoDao.siguienteCodigo(), cita, LocalDate.now(reloj),
                observaciones, recomendaciones, proximaAccion);
        seguimientoDao.guardar(s);
        return s;
    }

    public List<Seguimiento> listar() {
        return seguimientoDao.listar();
    }

    public List<Seguimiento> listarPorCita(String codigoCita) {
        return seguimientoDao.listarPorCita(codigoCita);
    }

    public List<Seguimiento> listarPorPaciente(String codigoPaciente) {
        return seguimientoDao.listarPorPaciente(codigoPaciente);
    }
}
