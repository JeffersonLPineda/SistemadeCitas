package com.citas.dao;

import com.citas.model.Paciente;
import java.util.List;
import java.util.Optional;

public interface PacienteDao {
    String siguienteCodigo();

    void guardar(Paciente paciente);

    Optional<Paciente> buscarPorCodigo(String codigo);

    List<Paciente> listar();
}
