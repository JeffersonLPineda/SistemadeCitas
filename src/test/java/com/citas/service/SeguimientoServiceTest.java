package com.citas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.citas.exception.CitaNoEncontradaException;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.EstadoCitaInvalidoException;
import com.citas.model.Cita;
import com.citas.model.Seguimiento;
import com.citas.tda.EventoHistorial;
import com.citas.tda.HistorialPaciente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeguimientoServiceTest {

    private Escenario e;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    private Cita citaProgramada() throws Exception {
        return e.citaService.programarCita(e.paciente1.getCodigo(), e.profesional.getCodigo(),
                Escenario.LUNES, Escenario.NUEVE, "Control");
    }

    private Cita citaAtendida() throws Exception {
        Cita c = citaProgramada();
        e.citaService.confirmarCita(c.getCodigo());
        e.citaService.atenderCita(c.getCodigo());
        return c;
    }

    // 7. Registrar seguimiento de una cita atendida
    @Test
    void registrarSeguimientoDeUnaCitaAtendida() throws Exception {
        Cita cita = citaAtendida();
        Seguimiento s = e.seguimientoService.registrar(cita.getCodigo(), "Evolución favorable",
                "Reposo e hidratación", "Control en 15 días");
        assertEquals(cita, s.getCita());
        assertEquals(Escenario.HOY, s.getFecha());
        assertEquals(1, e.seguimientoService.listarPorCita(cita.getCodigo()).size());
    }

    // 8. Impedir seguimiento si la cita aun no fue atendida
    @Test
    void impedirSeguimientoSiLaCitaNoFueAtendida() throws Exception {
        Cita programada = citaProgramada();
        assertThrows(EstadoCitaInvalidoException.class,
                () -> e.seguimientoService.registrar(programada.getCodigo(), "Obs", "Rec", "Acción"));

        e.citaService.confirmarCita(programada.getCodigo());
        assertThrows(EstadoCitaInvalidoException.class,
                () -> e.seguimientoService.registrar(programada.getCodigo(), "Obs", "Rec", "Acción"));
        assertTrue(e.seguimientoService.listar().isEmpty());
    }

    @Test
    void impedirSeguimientoDeUnaCitaCancelada() throws Exception {
        Cita cita = citaProgramada();
        e.citaService.cancelarCita(cita.getCodigo());
        assertThrows(EstadoCitaInvalidoException.class,
                () -> e.seguimientoService.registrar(cita.getCodigo(), "Obs", "", ""));
    }

    @Test
    void seguimientoExigeObservacionesYCitaExistente() throws Exception {
        Cita cita = citaAtendida();
        assertThrows(DatosInvalidosException.class,
                () -> e.seguimientoService.registrar(cita.getCodigo(), "  ", "Rec", "Acción"));
        assertThrows(CitaNoEncontradaException.class,
                () -> e.seguimientoService.registrar("CIT-9999", "Obs", "Rec", "Acción"));
    }

    @Test
    void historialEsCronologicoEIncluyeCitasYSeguimientos() throws Exception {
        Cita primera = citaAtendida();
        Cita segunda = e.citaService.programarCita(e.paciente1.getCodigo(), e.profesional.getCodigo(),
                Escenario.LUNES.plusDays(7), Escenario.NUEVE, "Revisión");
        e.seguimientoService.registrar(primera.getCodigo(), "Todo bien", "", "");

        HistorialService historial = new HistorialService(
                new PacienteService(e.pacienteDao, new UsuarioService(e.usuarioDao)), e.citaDao, e.seguimientoDao);
        HistorialPaciente h = historial.delPaciente(e.paciente1.getCodigo());
        List<EventoHistorial> eventos = h.getEventos();

        // El reloj de prueba esta fijo en el 10-oct, por eso el seguimiento (fecha de registro = hoy)
        // queda antes de las citas del 12 y del 19: lo importante es que el orden sea cronologico.
        assertEquals(3, eventos.size());
        assertEquals(EventoHistorial.Tipo.SEGUIMIENTO, eventos.get(0).getTipo());
        assertEquals(primera.getCodigo(), eventos.get(1).getCodigo());
        assertEquals(segunda.getCodigo(), eventos.get(2).getCodigo());
    }
}
