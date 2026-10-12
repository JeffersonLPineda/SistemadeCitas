package com.citas.service;

import com.citas.dao.ProfesionalDao;
import com.citas.exception.DatosInvalidosException;
import com.citas.model.Disponibilidad;
import com.citas.model.EstadoRegistro;
import com.citas.model.Profesional;
import com.citas.model.Rol;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfesionalService {

    private final ProfesionalDao profesionalDao;
    private final UsuarioService usuarioService;

    public ProfesionalService(ProfesionalDao profesionalDao, UsuarioService usuarioService) {
        this.profesionalDao = profesionalDao;
        this.usuarioService = usuarioService;
    }

    /** Registra un profesional; si se envian correo y contrasena tambien crea su usuario de acceso. */
    public Profesional registrar(String nombre, String especialidad, Set<DayOfWeek> dias, LocalTime inicio,
                                 LocalTime fin, String correoAcceso, String passwordInicial)
            throws DatosInvalidosException {
        Profesional p = new Profesional(profesionalDao.siguienteCodigo(), nombre, especialidad,
                new Disponibilidad(dias, inicio, fin));
        if (passwordInicial != null && !passwordInicial.isEmpty()) {
            usuarioService.crear(p.getNombre(), correoAcceso, passwordInicial, Rol.PROFESIONAL, p.getCodigo());
        }
        profesionalDao.guardar(p);
        return p;
    }

    public Profesional actualizar(String codigo, String nombre, String especialidad, Set<DayOfWeek> dias,
                                  LocalTime inicio, LocalTime fin) throws DatosInvalidosException {
        Profesional p = obtener(codigo);
        p.actualizarDatos(nombre, especialidad, new Disponibilidad(dias, inicio, fin));
        profesionalDao.guardar(p);
        return p;
    }

    public void cambiarEstado(String codigo, EstadoRegistro estado) throws DatosInvalidosException {
        Profesional p = obtener(codigo);
        p.setEstado(estado);
        profesionalDao.guardar(p);
    }

    public Profesional obtener(String codigo) throws DatosInvalidosException {
        return profesionalDao.buscarPorCodigo(codigo)
                .orElseThrow(() -> new DatosInvalidosException("El profesional indicado no existe."));
    }

    public List<Profesional> listar() {
        return profesionalDao.listar();
    }

    public List<Profesional> listarActivos() {
        return profesionalDao.listar().stream().filter(Profesional::isActivo).collect(Collectors.toList());
    }
}
