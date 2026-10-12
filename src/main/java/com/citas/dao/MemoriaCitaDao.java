package com.citas.dao;

import com.citas.exception.CitaNoEncontradaException;
import com.citas.exception.DatosInvalidosException;
import com.citas.model.Cita;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MemoriaCitaDao implements CitaDao {

    private static final Comparator<Cita> ORDEN =
            Comparator.comparing(Cita::getFechaHora).thenComparing(Cita::getCodigo);

    private final Map<String, Cita> datos = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    @Override
    public String siguienteCodigo() {
        return String.format("CIT-%04d", secuencia.incrementAndGet());
    }

    @Override
    public synchronized void guardar(Cita cita) throws DatosInvalidosException {
        if (datos.containsKey(cita.getCodigo())) {
            throw new DatosInvalidosException("El código de cita " + cita.getCodigo() + " ya existe (los códigos deben ser únicos).");
        }
        datos.put(cita.getCodigo(), cita);
    }

    @Override
    public synchronized void actualizar(Cita cita) throws CitaNoEncontradaException {
        if (!datos.containsKey(cita.getCodigo())) {
            throw new CitaNoEncontradaException(cita.getCodigo());
        }
        datos.put(cita.getCodigo(), cita);
    }

    @Override
    public Optional<Cita> buscarPorCodigo(String codigo) {
        return codigo == null ? Optional.empty() : Optional.ofNullable(datos.get(codigo.trim()));
    }

    @Override
    public List<Cita> listar() {
        List<Cita> lista = new ArrayList<>(datos.values());
        lista.sort(ORDEN);
        return lista;
    }

    @Override
    public List<Cita> listarPorPaciente(String codigoPaciente) {
        return listar().stream().filter(c -> c.getPaciente().getCodigo().equals(codigoPaciente))
                .collect(Collectors.toList());
    }

    @Override
    public List<Cita> listarPorProfesional(String codigoProfesional) {
        return listar().stream().filter(c -> c.getProfesional().getCodigo().equals(codigoProfesional))
                .collect(Collectors.toList());
    }
}
