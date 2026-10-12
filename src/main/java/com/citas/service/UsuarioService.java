package com.citas.service;

import com.citas.dao.UsuarioDao;
import com.citas.exception.DatosInvalidosException;
import com.citas.model.EstadoRegistro;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import com.citas.util.PasswordUtil;
import java.util.List;

public class UsuarioService {

    public static final int LONGITUD_MINIMA_PASSWORD = 8;

    private final UsuarioDao usuarioDao;

    public UsuarioService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * Crea una cuenta de acceso.
     * Precondiciones: datos validos, password con minimo 8 caracteres, correo no registrado.
     * Postcondicion: usuario guardado con la contrasena hasheada.
     */
    public Usuario crear(String nombre, String correo, String password, Rol rol, String codigoAsociado)
            throws DatosInvalidosException {
        validarPassword(password);
        Usuario u = new Usuario(0, nombre, correo, PasswordUtil.hashear(password), rol, codigoAsociado);
        return usuarioDao.guardar(u);
    }

    public void cambiarEstado(int id, EstadoRegistro estado, int idActor) throws DatosInvalidosException {
        Usuario u = obtener(id);
        if (id == idActor && estado == EstadoRegistro.INACTIVO) {
            throw new DatosInvalidosException("No puede desactivar su propia cuenta.");
        }
        u.setEstado(estado);
        usuarioDao.guardar(u);
    }

    public void restablecerPassword(int id, String nuevaPassword) throws DatosInvalidosException {
        validarPassword(nuevaPassword);
        Usuario u = obtener(id);
        u.setPasswordHash(PasswordUtil.hashear(nuevaPassword));
        usuarioDao.guardar(u);
    }

    public Usuario obtener(int id) throws DatosInvalidosException {
        return usuarioDao.buscarPorId(id).orElseThrow(() -> new DatosInvalidosException("El usuario no existe."));
    }

    public List<Usuario> listar() {
        return usuarioDao.listar();
    }

    private void validarPassword(String password) throws DatosInvalidosException {
        if (password == null || password.length() < LONGITUD_MINIMA_PASSWORD) {
            throw new DatosInvalidosException("La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres.");
        }
    }
}
