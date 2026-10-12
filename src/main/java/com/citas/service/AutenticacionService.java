package com.citas.service;

import com.citas.dao.UsuarioDao;
import com.citas.exception.AutenticacionException;
import com.citas.model.Usuario;
import com.citas.util.PasswordUtil;

/** Autenticacion de usuarios contra el DAO (las contrasenas se comparan siempre contra su hash). */
public class AutenticacionService {

    private static final String MENSAJE = "Correo o contraseña incorrectos.";
    // Hash ficticio: se verifica aunque el correo no exista, para no revelar por tiempo si el usuario existe.
    private static final String HASH_FALSO = PasswordUtil.hashear("no-existe-este-usuario");

    private final UsuarioDao usuarioDao;

    public AutenticacionService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * Precondicion: correo y password no nulos.
     * Postcondicion: devuelve el usuario activo cuyas credenciales coinciden.
     * @throws AutenticacionException si las credenciales son incorrectas o el usuario esta inactivo.
     */
    public Usuario autenticar(String correo, String password) throws AutenticacionException {
        Usuario usuario = usuarioDao.buscarPorCorreo(correo).orElse(null);
        String hash = usuario != null ? usuario.getPasswordHash() : HASH_FALSO;
        boolean coincide = PasswordUtil.verificar(password, hash);
        if (usuario == null || !coincide) {
            throw new AutenticacionException(MENSAJE);
        }
        if (!usuario.isActivo()) {
            throw new AutenticacionException("Su usuario está inactivo. Comuníquese con el administrador.");
        }
        return usuario;
    }
}
