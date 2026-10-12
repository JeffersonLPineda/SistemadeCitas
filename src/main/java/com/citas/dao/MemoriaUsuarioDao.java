package com.citas.dao;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Usuario;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Implementacion en memoria (se pierde al reiniciar). Para persistir en BD basta otra clase que implemente UsuarioDao. */
public class MemoriaUsuarioDao implements UsuarioDao {

    private final Map<Integer, Usuario> porId = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger();

    @Override
    public synchronized Usuario guardar(Usuario usuario) throws DatosInvalidosException {
        Optional<Usuario> existente = buscarPorCorreo(usuario.getCorreo());
        if (existente.isPresent() && existente.get().getId() != usuario.getId()) {
            throw new DatosInvalidosException("Ya existe un usuario con el correo " + usuario.getCorreo() + ".");
        }
        if (usuario.getId() == 0) {
            usuario.setId(secuencia.incrementAndGet());
        }
        porId.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        String c = correo.trim().toLowerCase();
        return porId.values().stream().filter(u -> u.getCorreo().equals(c)).findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>(porId.values());
        lista.sort(Comparator.comparingInt(Usuario::getId));
        return lista;
    }
}
