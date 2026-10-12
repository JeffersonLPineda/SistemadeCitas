package com.citas.dao;

import com.citas.model.Seguimiento;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MemoriaSeguimientoDao implements SeguimientoDao {

    private final List<Seguimiento> datos = new CopyOnWriteArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    @Override
    public String siguienteCodigo() {
        return String.format("SEG-%04d", secuencia.incrementAndGet());
    }

    @Override
    public void guardar(Seguimiento seguimiento) {
        datos.add(seguimiento);
    }

    @Override
    public List<Seguimiento> listar() {
        return datos.stream()
                .sorted(Comparator.comparing(Seguimiento::getFecha).thenComparing(Seguimiento::getCodigo))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seguimiento> listarPorCita(String codigoCita) {
        return listar().stream().filter(s -> s.getCita().getCodigo().equals(codigoCita)).collect(Collectors.toList());
    }

    @Override
    public List<Seguimiento> listarPorPaciente(String codigoPaciente) {
        return listar().stream().filter(s -> s.getCita().getPaciente().getCodigo().equals(codigoPaciente))
                .collect(Collectors.toList());
    }
}
