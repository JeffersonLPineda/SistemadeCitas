package com.citas.service;

import com.citas.dao.PacienteDao;
import com.citas.exception.DatosInvalidosException;
import com.citas.model.EstadoRegistro;
import com.citas.model.Paciente;
import com.citas.model.Rol;
import java.util.List;
import java.util.stream.Collectors;

public class PacienteService {

    private final PacienteDao pacienteDao;
    private final UsuarioService usuarioService;

    public PacienteService(PacienteDao pacienteDao, UsuarioService usuarioService) {
        this.pacienteDao = pacienteDao;
        this.usuarioService = usuarioService;
    }

    /**
     * Registra un paciente. Si se indica una contrasena inicial tambien crea su usuario de acceso
     * (el correo del paciente se usa como nombre de usuario).
     */
    public Paciente registrar(String nombre, String telefono, String correo, String direccion, String passwordInicial)
            throws DatosInvalidosException {
        Paciente p = new Paciente(pacienteDao.siguienteCodigo(), nombre, telefono, correo, direccion);
        if (passwordInicial != null && !passwordInicial.isEmpty()) {
            usuarioService.crear(p.getNombre(), p.getCorreo(), passwordInicial, Rol.PACIENTE, p.getCodigo());
        }
        pacienteDao.guardar(p);
        return p;
    }

    public Paciente actualizar(String codigo, String nombre, String telefono, String correo, String direccion)
            throws DatosInvalidosException {
        Paciente p = obtener(codigo);
        p.actualizarDatos(nombre, telefono, correo, direccion);
        pacienteDao.guardar(p);
        return p;
    }

    public void cambiarEstado(String codigo, EstadoRegistro estado) throws DatosInvalidosException {
        Paciente p = obtener(codigo);
        p.setEstado(estado);
        pacienteDao.guardar(p);
    }

    public Paciente obtener(String codigo) throws DatosInvalidosException {
        return pacienteDao.buscarPorCodigo(codigo)
                .orElseThrow(() -> new DatosInvalidosException("El paciente indicado no existe."));
    }

    public List<Paciente> listar() {
        return pacienteDao.listar();
    }

    public List<Paciente> listarActivos() {
        return pacienteDao.listar().stream().filter(Paciente::isActivo).collect(Collectors.toList());
    }
}
