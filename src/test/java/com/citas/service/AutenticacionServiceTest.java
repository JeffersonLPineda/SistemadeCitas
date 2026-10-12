package com.citas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.citas.exception.AutenticacionException;
import com.citas.exception.DatosInvalidosException;
import com.citas.model.EstadoRegistro;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticacionServiceTest {

    private Escenario e;
    private UsuarioService usuarios;
    private AutenticacionService auth;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
        usuarios = new UsuarioService(e.usuarioDao);
        auth = new AutenticacionService(e.usuarioDao);
        usuarios.crear("Ana Prueba", "ana@test.com", "Clave#12345", Rol.PACIENTE, e.paciente1.getCodigo());
    }

    @Test
    void autenticaConCredencialesCorrectas() throws Exception {
        Usuario u = auth.autenticar("ANA@test.com", "Clave#12345");
        assertEquals(Rol.PACIENTE, u.getRol());
        assertEquals(e.paciente1.getCodigo(), u.getCodigoAsociado());
    }

    @Test
    void rechazaContrasenaIncorrectaOUsuarioInexistente() {
        assertThrows(AutenticacionException.class, () -> auth.autenticar("ana@test.com", "otra-clave"));
        assertThrows(AutenticacionException.class, () -> auth.autenticar("nadie@test.com", "Clave#12345"));
    }

    @Test
    void rechazaUsuariosInactivos() throws Exception {
        Usuario u = e.usuarioDao.buscarPorCorreo("ana@test.com").get();
        usuarios.cambiarEstado(u.getId(), EstadoRegistro.INACTIVO, -1);
        assertThrows(AutenticacionException.class, () -> auth.autenticar("ana@test.com", "Clave#12345"));
    }

    @Test
    void exigeContrasenaMinimaYCorreoUnico() {
        assertThrows(DatosInvalidosException.class,
                () -> usuarios.crear("Otro", "otro@test.com", "corta", Rol.ADMINISTRADOR, null));
        assertThrows(DatosInvalidosException.class,
                () -> usuarios.crear("Otro", "ana@test.com", "Clave#12345", Rol.PACIENTE, "PAC-0002"));
    }
}
