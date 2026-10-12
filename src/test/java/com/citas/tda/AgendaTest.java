package com.citas.tda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.citas.exception.HorarioNoDisponibleException;
import com.citas.model.Cita;
import com.citas.model.Disponibilidad;
import com.citas.model.Paciente;
import com.citas.model.Profesional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class AgendaTest {

    private final LocalDate dia = LocalDate.of(2026, 10, 12);

    private Cita cita(String codigo, int hora) throws Exception {
        Paciente p = new Paciente("PAC-1", "Ana", "", "ana@test.com", "");
        Profesional pr = new Profesional("PRO-1", "Dr. X", "General",
                new Disponibilidad(EnumSet.allOf(DayOfWeek.class), LocalTime.of(8, 0), LocalTime.of(17, 0)));
        return new Cita(codigo, p, pr, dia, LocalTime.of(hora, 0), "Control");
    }

    @Test
    void insertaYDevuelveLasCitasOrdenadas() throws Exception {
        Agenda agenda = new Agenda();
        agenda.insertar(cita("C3", 11));
        agenda.insertar(cita("C1", 9));
        agenda.insertar(cita("C2", 10));
        assertEquals(3, agenda.tamano());
        assertEquals("C1", agenda.todas().get(0).getCodigo());
        assertEquals("C3", agenda.todas().get(2).getCodigo());
        assertEquals(3, agenda.citasDelDia(dia).size());
        assertTrue(agenda.citasDelDia(dia.plusDays(1)).isEmpty());
    }

    @Test
    void noPermiteDosCitasEnElMismoHorario() throws Exception {
        Agenda agenda = new Agenda();
        agenda.insertar(cita("C1", 9));
        assertThrows(HorarioNoDisponibleException.class, () -> agenda.insertar(cita("C2", 9)));
        assertEquals(1, agenda.tamano());
        assertTrue(agenda.estaOcupado(dia, LocalTime.of(9, 0)));
        assertFalse(agenda.estaOcupado(dia, LocalTime.of(10, 0)));
    }

    @Test
    void desdeIgnoraCitasCanceladasYLiberaElHorario() throws Exception {
        Cita activa = cita("C1", 9);
        Cita cancelada = cita("C2", 9);
        cancelada.cancelar();
        Agenda agenda = Agenda.desde(Arrays.asList(cancelada, activa));
        assertEquals(1, agenda.tamano());
        assertTrue(agenda.liberar(activa));
        assertTrue(agenda.estaVacia());
        assertFalse(agenda.obtener(dia, LocalTime.of(9, 0)).isPresent());
    }
}
