package com.citas.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.citas.config.Esquema;
import com.citas.config.FuenteDatos;
import com.citas.config.Repositorios;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.HorarioNoDisponibleException;
import com.citas.model.Cita;
import com.citas.model.Disponibilidad;
import com.citas.model.EstadoCita;
import com.citas.model.Paciente;
import com.citas.model.Profesional;
import com.citas.model.Rol;
import com.citas.model.Seguimiento;
import com.citas.model.Usuario;
import com.citas.notification.Notificador;
import com.citas.service.CitaService;
import com.citas.service.SeguimientoService;
import com.citas.tda.HistorialPaciente;
import com.citas.service.HistorialService;
import com.citas.service.PacienteService;
import com.citas.service.UsuarioService;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba de integracion contra PostgreSQL. Se OMITE si no se define TEST_DB_URL.
 * Use una base de pruebas, no la de la aplicacion:
 *   TEST_DB_URL=jdbc:postgresql://localhost:5432/gestion_citas_test  TEST_DB_USER=...  TEST_DB_PASSWORD=...  mvn test
 */
class JdbcPersistenciaTest {

    private HikariDataSource pool;
    private Repositorios repos;
    private CitaService citas;
    private Paciente paciente;
    private Profesional profesional;
    private final Clock reloj = Clock.systemDefaultZone();

    @BeforeEach
    void preparar() throws Exception {
        String url = System.getenv("TEST_DB_URL");
        assumeTrue(url != null && !url.isEmpty(), "Defina TEST_DB_URL para ejecutar las pruebas con PostgreSQL");
        String user = System.getenv("TEST_DB_USER");
        String pass = System.getenv("TEST_DB_PASSWORD");
        pool = FuenteDatos.crear(url, user == null ? "" : user, pass == null ? "" : pass);
        Esquema.crear(pool);
        repos = Repositorios.jdbc(pool);
        citas = new CitaService(repos.citas(), repos.pacientes(), repos.profesionales(),
                new Notificador(Collections.emptyList()), reloj);

        paciente = new Paciente(repos.pacientes().siguienteCodigo(), "Paciente Prueba", "5555-0000",
                "p" + UUID.randomUUID() + "@test.com", "Zona 1");
        repos.pacientes().guardar(paciente);
        profesional = new Profesional(repos.profesionales().siguienteCodigo(), "Prof. Prueba", "General",
                new Disponibilidad(EnumSet.allOf(DayOfWeek.class), LocalTime.of(0, 0), LocalTime.of(23, 30)));
        repos.profesionales().guardar(profesional);
    }

    @AfterEach
    void cerrar() {
        if (pool != null) {
            pool.close();
        }
    }

    private LocalDate fechaFutura() {
        return LocalDate.now(reloj).plusDays(400 + (long) (Math.random() * 3000));
    }

    @Test
    void lasCitasYSeguimientosSePersistenYSeRecuperan() throws Exception {
        LocalDate fecha = fechaFutura();
        Cita c = citas.programarCita(paciente.getCodigo(), profesional.getCodigo(), fecha, LocalTime.of(9, 0), "Control");
        citas.confirmarCita(c.getCodigo());
        citas.atenderCita(c.getCodigo());
        Seguimiento s = new SeguimientoService(repos.seguimientos(), citas, reloj)
                .registrar(c.getCodigo(), "Todo bien", "Descanso", "Control en 1 mes");

        Cita leida = repos.citas().buscarPorCodigo(c.getCodigo()).get();
        assertEquals(EstadoCita.ATENDIDA, leida.getEstado());
        assertEquals(fecha, leida.getFecha());
        assertEquals(paciente.getCodigo(), leida.getPaciente().getCodigo());
        assertEquals(1, repos.seguimientos().listarPorCita(c.getCodigo()).size());
        assertEquals(s.getCodigo(), repos.seguimientos().listarPorPaciente(paciente.getCodigo()).get(0).getCodigo());

        HistorialPaciente h = new HistorialService(
                new PacienteService(repos.pacientes(), new UsuarioService(repos.usuarios())),
                repos.citas(), repos.seguimientos()).delPaciente(paciente.getCodigo());
        assertEquals(2, h.tamano());
    }

    @Test
    void laBaseImpideDobleReservaDelProfesionalYCancelarLiberaElHorario() throws Exception {
        LocalDate fecha = fechaFutura();
        Cita primera = Cita.restaurar(repos.citas().siguienteCodigo(), paciente, profesional, fecha,
                LocalTime.of(10, 0), "Primera", EstadoCita.PROGRAMADA);
        repos.citas().guardar(primera);

        Cita duplicada = new Cita(repos.citas().siguienteCodigo(), paciente, profesional, fecha, LocalTime.of(10, 0), "Duplicada");
        assertThrows(HorarioNoDisponibleException.class, () -> repos.citas().guardar(duplicada));

        primera.cancelar();
        repos.citas().actualizar(primera);
        repos.citas().guardar(duplicada); // el horario ya esta libre
        assertTrue(repos.citas().buscarPorCodigo(duplicada.getCodigo()).isPresent());
    }

    @Test
    void elCorreoDeUsuarioEsUnico() throws Exception {
        String correo = "u" + UUID.randomUUID() + "@test.com";
        repos.usuarios().guardar(new Usuario(0, "Uno", correo, "hash", Rol.ADMINISTRADOR, null));
        assertThrows(DatosInvalidosException.class,
                () -> repos.usuarios().guardar(new Usuario(0, "Dos", correo, "hash", Rol.ADMINISTRADOR, null)));
    }
}
