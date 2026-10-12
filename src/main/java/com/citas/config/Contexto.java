package com.citas.config;

import com.citas.exception.GestionCitasException;
import com.citas.notification.BitacoraNotificaciones;
import com.citas.notification.Notificable;
import com.citas.notification.NotificacionCorreo;
import com.citas.notification.NotificacionSMS;
import com.citas.notification.Notificador;
import com.citas.service.AutenticacionService;
import com.citas.service.CitaService;
import com.citas.service.HistorialService;
import com.citas.service.PacienteService;
import com.citas.service.ProfesionalService;
import com.citas.service.RecordatorioService;
import com.citas.service.SeguimientoService;
import com.citas.service.UsuarioService;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Raiz de composicion: crea los DAO y los servicios una sola vez y los conecta entre si
 * (inyeccion de dependencias manual). Los servlets obtienen los servicios desde aqui.
 */
public final class Contexto {

    private static final Logger LOG = Logger.getLogger(Contexto.class.getName());
    private static Contexto instancia;
    private static AutoCloseable recursos; // pool de conexiones (si se usa PostgreSQL)

    private final BitacoraNotificaciones bitacora = new BitacoraNotificaciones();
    private final AutenticacionService autenticacion;
    private final UsuarioService usuarios;
    private final PacienteService pacientes;
    private final ProfesionalService profesionales;
    private final CitaService citas;
    private final SeguimientoService seguimientos;
    private final HistorialService historial;
    private final RecordatorioService recordatorios;

    /**
     * Instancia unica de la aplicacion. Segun db.properties usa PostgreSQL (crea las tablas si no existen)
     * o la persistencia en memoria.
     */
    public static synchronized Contexto instancia() {
        if (instancia == null) {
            Configuracion cfg = Configuracion.cargar();
            Repositorios repos;
            if ("memoria".equalsIgnoreCase(cfg.persistencia())) {
                LOG.warning("Persistencia EN MEMORIA: los datos se perderán al reiniciar el servidor.");
                repos = Repositorios.enMemoria();
            } else {
                com.zaxxer.hikari.HikariDataSource pool = FuenteDatos.crear(cfg);
                try {
                    Esquema.crear(pool);
                } catch (RuntimeException e) {
                    pool.close();
                    throw e;
                }
                recursos = pool;
                repos = Repositorios.jdbc((DataSource) pool);
                LOG.info("Persistencia en PostgreSQL: " + cfg.url());
            }
            try {
                instancia = new Contexto(Clock.systemDefaultZone(), repos, cfg.datosDemo());
            } catch (RuntimeException e) {
                cerrar();
                throw e;
            }
        }
        return instancia;
    }

    /** Libera el pool de conexiones (se llama al detener la aplicacion). */
    public static synchronized void cerrar() {
        if (recursos != null) {
            try {
                recursos.close();
            } catch (Exception e) {
                LOG.warning("No se pudo cerrar el pool de conexiones: " + e.getMessage());
            }
            recursos = null;
        }
        instancia = null;
    }

    /** Version en memoria (usada por pruebas y demostraciones rapidas). */
    public Contexto(Clock reloj, boolean cargarDatosDemostracion) {
        this(reloj, Repositorios.enMemoria(), cargarDatosDemostracion);
    }

    public Contexto(Clock reloj, Repositorios repos, boolean cargarDatosDemostracion) {
        Notificador notificador = new Notificador(Arrays.<Notificable>asList(
                new NotificacionCorreo(bitacora), new NotificacionSMS(bitacora)));

        autenticacion = new AutenticacionService(repos.usuarios());
        usuarios = new UsuarioService(repos.usuarios());
        pacientes = new PacienteService(repos.pacientes(), usuarios);
        profesionales = new ProfesionalService(repos.profesionales(), usuarios);
        citas = new CitaService(repos.citas(), repos.pacientes(), repos.profesionales(), notificador, reloj);
        seguimientos = new SeguimientoService(repos.seguimientos(), citas, reloj);
        historial = new HistorialService(pacientes, repos.citas(), repos.seguimientos());
        recordatorios = new RecordatorioService(citas, notificador);

        // Datos de demostracion solo si la base esta vacia (no se duplican al reiniciar)
        if (cargarDatosDemostracion && repos.usuarios().listar().isEmpty()) {
            try {
                DatosIniciales.cargar(repos.usuarios(), repos.pacientes(), repos.profesionales(), repos.citas(),
                        repos.seguimientos(), reloj);
            } catch (GestionCitasException e) {
                throw new IllegalStateException("No se pudieron cargar los datos iniciales", e);
            }
        }
    }

    public BitacoraNotificaciones bitacora() { return bitacora; }
    public AutenticacionService autenticacion() { return autenticacion; }
    public UsuarioService usuarios() { return usuarios; }
    public PacienteService pacientes() { return pacientes; }
    public ProfesionalService profesionales() { return profesionales; }
    public CitaService citas() { return citas; }
    public SeguimientoService seguimientos() { return seguimientos; }
    public HistorialService historial() { return historial; }
    public RecordatorioService recordatorios() { return recordatorios; }
}
