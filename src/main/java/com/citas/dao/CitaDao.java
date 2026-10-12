package com.citas.dao;

import com.citas.exception.CitaNoEncontradaException;
import com.citas.exception.GestionCitasException;
import com.citas.model.Cita;
import java.util.List;
import java.util.Optional;

public interface CitaDao {
    String siguienteCodigo();

    /**
     * Inserta una cita nueva.
     * @throws com.citas.exception.DatosInvalidosException si el codigo ya existe (codigos unicos).
     * @throws com.citas.exception.HorarioNoDisponibleException si el profesional ya tiene una cita vigente en ese horario.
     */
    void guardar(Cita cita) throws GestionCitasException;

    /** Persiste los cambios de una cita existente. @throws CitaNoEncontradaException si no existe. */
    void actualizar(Cita cita) throws CitaNoEncontradaException;

    Optional<Cita> buscarPorCodigo(String codigo);

    /** Todas las citas ordenadas por fecha, hora y codigo. */
    List<Cita> listar();

    List<Cita> listarPorPaciente(String codigoPaciente);

    List<Cita> listarPorProfesional(String codigoProfesional);
}
