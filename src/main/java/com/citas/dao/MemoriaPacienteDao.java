package com.citas.dao;

import com.citas.model.Paciente;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoriaPacienteDao implements PacienteDao {

    private final Map<String, Paciente> datos = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    @Override
    public String siguienteCodigo() {
        return String.format("PAC-%04d", secuencia.incrementAndGet());
    }

    @Override
    public void guardar(Paciente paciente) {
        datos.put(paciente.getCodigo(), paciente);
    }

    @Override
    public Optional<Paciente> buscarPorCodigo(String codigo) {
        return codigo == null ? Optional.empty() : Optional.ofNullable(datos.get(codigo.trim()));
    }

    @Override
    public List<Paciente> listar() {
        List<Paciente> lista = new ArrayList<>(datos.values());
        lista.sort(Comparator.comparing(Paciente::getCodigo));
        return lista;
    }
}
