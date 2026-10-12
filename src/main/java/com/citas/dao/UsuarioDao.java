package com.citas.dao;

import com.citas.exception.DatosInvalidosException;
import com.citas.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    /** Inserta (id == 0 asigna uno nuevo) o actualiza. El correo debe ser unico. */
    Usuario guardar(Usuario usuario) throws DatosInvalidosException;

    Optional<Usuario> buscarPorCorreo(String correo);

    Optional<Usuario> buscarPorId(int id);

    List<Usuario> listar();
}
