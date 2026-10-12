package com.citas.service;

import com.citas.dao.MemoriaCitaDao;
import com.citas.dao.MemoriaPacienteDao;
import com.citas.dao.MemoriaProfesionalDao;
import com.citas.dao.MemoriaSeguimientoDao;
import com.citas.dao.MemoriaUsuarioDao;
import com.citas.model.Disponibilidad;
import com.citas.model.Paciente;
import com.citas.model.Profesional;
import com.citas.notification.BitacoraNotificaciones;
import com.citas.notification.Notificable;
import com.citas.notification.NotificacionCorreo;
import com.citas.notification.NotificacionSMS;
import com.citas.notification.Notificador;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/** Escenario comun de pruebas: DAOs en memoria, reloj fijo (sabado 2026-10-10, 15:00 UTC) y datos base. */
class Escenario {

    static final LocalDate HOY = LocalDate.of(2026, 10, 10);
    static final LocalDate LUNES = LocalDate.of(2026, 10, 12);
    static final LocalTime NUEVE = LocalTime.of(9, 0);

    final Clock reloj = Clock.fixed(Instant.parse("2026-10-10T15:00:00Z"), ZoneOffset.UTC);
    final MemoriaCitaDao citaDao = new MemoriaCitaDao();
    final MemoriaPacienteDao pacienteDao = new MemoriaPacienteDao();
    final MemoriaProfesionalDao profesionalDao = new MemoriaProfesionalDao();
    final MemoriaSeguimientoDao seguimientoDao = new MemoriaSeguimientoDao();
    final MemoriaUsuarioDao usuarioDao = new MemoriaUsuarioDao();
    final BitacoraNotificaciones bitacora = new BitacoraNotificaciones();
    final CitaService citaService;
    final SeguimientoService seguimientoService;
    final Paciente paciente1;
    final Paciente paciente2;
    final Profesional profesional;

    Escenario() throws Exception {
        List<Notificable> canales = Arrays.asList(new NotificacionCorreo(bitacora), new NotificacionSMS(bitacora));
        citaService = new CitaService(citaDao, pacienteDao, profesionalDao, new Notificador(canales), reloj);
        seguimientoService = new SeguimientoService(seguimientoDao, citaService, reloj);

        paciente1 = new Paciente(pacienteDao.siguienteCodigo(), "Ana Prueba", "5555-1234", "ana@test.com", "Zona 1");
        paciente2 = new Paciente(pacienteDao.siguienteCodigo(), "Luis Prueba", "5555-9876", "luis@test.com", "Zona 2");
        pacienteDao.guardar(paciente1);
        pacienteDao.guardar(paciente2);

        profesional = new Profesional(profesionalDao.siguienteCodigo(), "Dra. Test", "General",
                new Disponibilidad(EnumSet.allOf(DayOfWeek.class), LocalTime.of(8, 0), LocalTime.of(17, 0)));
        profesionalDao.guardar(profesional);
    }
}
