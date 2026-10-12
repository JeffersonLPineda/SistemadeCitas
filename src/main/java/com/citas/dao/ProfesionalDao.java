package com.citas.dao;

import com.citas.model.Profesional;
import java.util.List;
import java.util.Optional;

public interface ProfesionalDao {
    String siguienteCodigo();

    void guardar(Profesional profesional);

    Optional<Profesional> buscarPorCodigo(String codigo);

    List<Profesional> listar();
}
