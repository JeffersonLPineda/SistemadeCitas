package com.citas.dao;

import com.citas.model.Seguimiento;
import java.util.List;

public interface SeguimientoDao {
    String siguienteCodigo();

    void guardar(Seguimiento seguimiento);

    List<Seguimiento> listar();

    List<Seguimiento> listarPorCita(String codigoCita);

    List<Seguimiento> listarPorPaciente(String codigoPaciente);
}
