package com.citas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.citas.exception.CitaNoEncontradaException;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.EstadoCitaInvalidoException;
import com.citas.exception.FechaInvalidaException;
import com.citas.exception.HorarioNoDisponibleException;
import com.citas.model.Cita;
import com.citas.model.EstadoCita;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CitaServiceTest {

    private Escenario e;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    private Cita programar() throws Exception {
        return e.citaService.programarCita(e.paciente1.getCodigo(), e.profesional.getCodigo(),
                Escenario.LUNES, Escenario.NUEVE, "Control general");
    }

    // 1. Programar correctamente una cita
    @Test
    void programarCorrectamenteUnaCita() throws Exception {
        Cita cita = programar();
        assertNotNull(cita.getCodigo());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals(e.paciente1, cita.getPaciente());
        assertEquals(e.profesional, cita.getProfesional());
        assertTrue(e.citaDao.buscarPorCodigo(cita.getCodigo()).isPresent());
        assertFalse(e.bitacora.ultimas(5).isEmpty(), "Debe registrarse la notificación de la cita");
    }

    // 2. Rechazar una fecha invalida
    @Test
    void rechazarFechaAnteriorAHoy() {
        assertThrows(FechaInvalidaException.class, () -> e.citaService.programarCita(
                e.paciente1.getCodigo(), e.profesional.getCodigo(), Escenario.HOY.minusDays(1), Escenario.NUEVE, "Control"));
        assertTrue(e.citaDao.listar().isEmpty());
    }

    @Test
    void rechazarHoraYaPasadaDeHoy() {
        // El reloj fijo marca las 15:00; las 09:00 de hoy ya pasaron
        assertThrows(FechaInvalidaException.class, () -> e.citaService.programarCita(
                e.paciente1.getCodigo(), e.profesional.getCodigo(), Escenario.HOY, Escenario.NUEVE, "Control"));
    }

    // 3. Evitar duplicidad de horario para un profesional
    @Test
    void evitarDuplicidadDeHorarioDelProfesional() throws Exception {
        programar();
        assertThrows(HorarioNoDisponibleException.class, () -> e.citaService.programarCita(
                e.paciente2.getCodigo(), e.profesional.getCodigo(), Escenario.LUNES, Escenario.NUEVE, "Otra consulta"));
        assertEquals(1, e.citaDao.listar().size());
    }

    @Test
    void cancelarLiberaElHorario() throws Exception {
        Cita primera = programar();
        e.citaService.cancelarCita(primera.getCodigo());
        Cita segunda = e.citaService.programarCita(e.paciente2.getCodigo(), e.profesional.getCodigo(),
                Escenario.LUNES, Escenario.NUEVE, "Consulta");
        assertEquals(EstadoCita.PROGRAMADA, segunda.getEstado());
    }

    @Test
    void rechazarHorarioFueraDeDisponibilidadOFueraDeBloque() {
        assertThrows(HorarioNoDisponibleException.class, () -> e.citaService.programarCita(
                e.paciente1.getCodigo(), e.profesional.getCodigo(), Escenario.LUNES, LocalTime.of(20, 0), "Tarde"));
        assertThrows(HorarioNoDisponibleException.class, () -> e.citaService.programarCita(
                e.paciente1.getCodigo(), e.profesional.getCodigo(), Escenario.LUNES, LocalTime.of(9, 10), "Fuera de bloque"));
    }

    @Test
    void todaCitaDebeTenerPacienteYProfesional() {
        assertThrows(DatosInvalidosException.class, () -> e.citaService.programarCita(
                "PAC-9999", e.profesional.getCodigo(), Escenario.LUNES, Escenario.NUEVE, "Control"));
        assertThrows(DatosInvalidosException.class, () -> e.citaService.programarCita(
                e.paciente1.getCodigo(), "PRO-9999", Escenario.LUNES, Escenario.NUEVE, "Control"));
    }

    // 4. Cancelar una cita
    @Test
    void cancelarUnaCita() throws Exception {
        Cita cita = programar();
        e.citaService.cancelarCita(cita.getCodigo());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    }

    @Test
    void noSePuedeCancelarUnaCitaCancelada() throws Exception {
        Cita cita = programar();
        e.citaService.cancelarCita(cita.getCodigo());
        assertThrows(EstadoCitaInvalidoException.class, () -> e.citaService.cancelarCita(cita.getCodigo()));
    }

    // 5. Impedir atender una cita cancelada
    @Test
    void impedirAtenderUnaCitaCancelada() throws Exception {
        Cita cita = programar();
        e.citaService.cancelarCita(cita.getCodigo());
        assertThrows(EstadoCitaInvalidoException.class, () -> e.citaService.atenderCita(cita.getCodigo()));
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    }

    @Test
    void noSePuedeAtenderUnaCitaSinConfirmar() throws Exception {
        Cita cita = programar();
        assertThrows(EstadoCitaInvalidoException.class, () -> e.citaService.atenderCita(cita.getCodigo()));
    }

    // 6. Registrar una cita como atendida
    @Test
    void registrarUnaCitaComoAtendida() throws Exception {
        Cita cita = programar();
        e.citaService.confirmarCita(cita.getCodigo());
        e.citaService.atenderCita(cita.getCodigo());
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    }

    @Test
    void citaInexistenteLanzaCitaNoEncontrada() {
        assertThrows(CitaNoEncontradaException.class, () -> e.citaService.confirmarCita("CIT-9999"));
    }

    @Test
    void losCodigosDeCitaSonUnicos() throws Exception {
        Cita a = programar();
        Cita b = e.citaService.programarCita(e.paciente2.getCodigo(), e.profesional.getCodigo(),
                Escenario.LUNES, LocalTime.of(10, 0), "Otra");
        assertFalse(a.getCodigo().equals(b.getCodigo()));
        assertThrows(DatosInvalidosException.class, () -> e.citaDao.guardar(a));
    }

    @Test
    void recordatoriosSoloParaCitasVigentesDeLaFecha() throws Exception {
        Cita a = programar();
        Cita b = e.citaService.programarCita(e.paciente2.getCodigo(), e.profesional.getCodigo(),
                Escenario.LUNES, LocalTime.of(10, 0), "Otra");
        e.citaService.cancelarCita(b.getCodigo());
        RecordatorioService recordatorios = new RecordatorioService(e.citaService,
                new com.citas.notification.Notificador(java.util.Collections.singletonList(
                        new com.citas.notification.NotificacionCorreo(e.bitacora))));
        assertEquals(1, recordatorios.enviarRecordatorios(Escenario.LUNES));
        assertEquals(a.getFecha(), Escenario.LUNES);
    }
}
