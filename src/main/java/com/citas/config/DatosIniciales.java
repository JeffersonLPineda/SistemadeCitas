package com.citas.config;

import com.citas.dao.CitaDao;
import com.citas.dao.PacienteDao;
import com.citas.dao.ProfesionalDao;
import com.citas.dao.SeguimientoDao;
import com.citas.dao.UsuarioDao;
import com.citas.exception.GestionCitasException;
import com.citas.model.Cita;
import com.citas.model.Disponibilidad;
import com.citas.model.Paciente;
import com.citas.model.Profesional;
import com.citas.model.Rol;
import com.citas.model.Seguimiento;
import com.citas.model.Usuario;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Datos de demostracion. Las contrasenas NO aparecen en el codigo: solo se guardan sus hashes PBKDF2
 * (las credenciales de prueba se indican en el README y deben cambiarse antes de la entrega final).
 */
final class DatosIniciales {

    private static final String HASH_ADMIN = "pbkdf2$65536$7c87a69ec2a3619fcf4a379bbcc22cee$984118bbd3771ee624bb9e71b25c3c2ad26245a7bb3900bce6f9aa9f7259da49";
    private static final String HASH_PROF1 = "pbkdf2$65536$9c03ca3de99c6adffc7311ac5b21b335$f7f30431f39920484bca593b0e1327017e3a76d6d1cb03a0b6853d4b71a1190a";
    private static final String HASH_PROF2 = "pbkdf2$65536$792dfb9afd6f80fd883c4c6fb81d8aa1$21b609673bb0b59af24fde6a1359c9674ce3305b60c6b1e9886ff3c1de7b752a";
    private static final String HASH_PAC1 = "pbkdf2$65536$b35c86e6993e1dbc4bdaac65c0f44436$97e450aacc1c754be2975776b85eb1352091afd1b3ef9a934807b430ecd45f5f";
    private static final String HASH_PAC2 = "pbkdf2$65536$8e4e84734924e72471d764330e169a66$612fd25a9d160f9018f9f360a254bcb65735fc26f02f257a02948f7d0da2cf92";

    private DatosIniciales() {
    }

    static void cargar(UsuarioDao usuarios, PacienteDao pacientes, ProfesionalDao profesionales, CitaDao citas,
                       SeguimientoDao seguimientos, Clock reloj) throws GestionCitasException {
        Profesional lopez = new Profesional(profesionales.siguienteCodigo(), "Dra. Ana López", "Medicina General",
                new Disponibilidad(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), LocalTime.of(8, 0), LocalTime.of(16, 0)));
        Profesional perez = new Profesional(profesionales.siguienteCodigo(), "Dr. Carlos Pérez", "Odontología",
                new Disponibilidad(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                        LocalTime.of(9, 0), LocalTime.of(17, 0)));
        profesionales.guardar(lopez);
        profesionales.guardar(perez);

        Paciente maria = new Paciente(pacientes.siguienteCodigo(), "María Gómez", "5551-0001", "maria.gomez@citas.local", "Zona 1");
        Paciente juan = new Paciente(pacientes.siguienteCodigo(), "Juan Ramírez", "5551-0002", "juan.ramirez@citas.local", "Zona 2");
        pacientes.guardar(maria);
        pacientes.guardar(juan);

        usuarios.guardar(new Usuario(0, "Administrador", "admin@citas.local", HASH_ADMIN, Rol.ADMINISTRADOR, null));
        usuarios.guardar(new Usuario(0, lopez.getNombre(), "dra.lopez@citas.local", HASH_PROF1, Rol.PROFESIONAL, lopez.getCodigo()));
        usuarios.guardar(new Usuario(0, perez.getNombre(), "dr.perez@citas.local", HASH_PROF2, Rol.PROFESIONAL, perez.getCodigo()));
        usuarios.guardar(new Usuario(0, maria.getNombre(), maria.getCorreo(), HASH_PAC1, Rol.PACIENTE, maria.getCodigo()));
        usuarios.guardar(new Usuario(0, juan.getNombre(), juan.getCorreo(), HASH_PAC2, Rol.PACIENTE, juan.getCodigo()));

        LocalDate hoy = LocalDate.now(reloj);

        // Una cita ya atendida con su seguimiento (para mostrar el historial)
        Cita pasada = new Cita(citas.siguienteCodigo(), maria, lopez, hoy.minusDays(7), LocalTime.of(9, 0), "Control de presión arterial");
        pasada.confirmar();
        pasada.atender();
        citas.guardar(pasada);
        seguimientos.guardar(new Seguimiento(seguimientos.siguienteCodigo(), pasada, hoy.minusDays(7),
                "Presión estable, sin síntomas.", "Reducir el consumo de sal y caminar 30 minutos diarios.",
                "Control en un mes"));

        // Citas futuras (en dias en que el profesional atiende)
        Cita futura1 = new Cita(citas.siguienteCodigo(), juan, lopez,
                proximoDia(hoy.plusDays(1), lopez.getDisponibilidad().getDias()), LocalTime.of(10, 0), "Dolor de garganta");
        Cita futura2 = new Cita(citas.siguienteCodigo(), maria, perez,
                proximoDia(hoy.plusDays(1), perez.getDisponibilidad().getDias()), LocalTime.of(11, 30), "Limpieza dental");
        futura2.confirmar();
        citas.guardar(futura1);
        citas.guardar(futura2);
    }

    private static LocalDate proximoDia(LocalDate desde, Set<DayOfWeek> dias) {
        LocalDate d = desde;
        while (!dias.contains(d.getDayOfWeek())) {
            d = d.plusDays(1);
        }
        return d;
    }
}
