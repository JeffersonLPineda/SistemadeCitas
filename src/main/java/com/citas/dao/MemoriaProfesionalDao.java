package com.citas.dao;

import com.citas.model.Profesional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoriaProfesionalDao implements ProfesionalDao {

    private final Map<String, Profesional> datos = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    @Override
    public String siguienteCodigo() {
        return String.format("PRO-%04d", secuencia.incrementAndGet());
    }

    @Override
    public void guardar(Profesional profesional) {
        datos.put(profesional.getCodigo(), profesional);
    }

    @Override
    public Optional<Profesional> buscarPorCodigo(String codigo) {
        return codigo == null ? Optional.empty() : Optional.ofNullable(datos.get(codigo.trim()));
    }

    @Override
    public List<Profesional> listar() {
        List<Profesional> lista = new ArrayList<>(datos.values());
        lista.sort(Comparator.comparing(Profesional::getCodigo));
        return lista;
    }
}
