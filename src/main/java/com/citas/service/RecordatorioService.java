package com.citas.service;

import com.citas.model.Cita;
import com.citas.model.EstadoCita;
import com.citas.notification.Notificador;
import java.time.LocalDate;

/** Reto adicional: recordatorios de citas por los canales Notificable (correo y SMS). */
public class RecordatorioService {

    private final CitaService citaService;
    private final Notificador notificador;

    public RecordatorioService(CitaService citaService, Notificador notificador) {
        this.citaService = citaService;
        this.notificador = notificador;
    }

    /** Envia un recordatorio por cada cita PROGRAMADA o CONFIRMADA de la fecha. Devuelve cuantas encontro. */
    public int enviarRecordatorios(LocalDate fecha) {
        int enviados = 0;
        for (Cita c : citaService.listar()) {
            boolean vigente = c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.CONFIRMADA;
            if (vigente && c.getFecha().equals(fecha)) {
                notificador.avisar(c.getPaciente(), "Recordatorio: tiene la cita " + c.getCodigo() + " con "
                        + c.getProfesional().getNombre() + " el " + c.getFecha() + " a las " + c.getHora() + ".");
                enviados++;
            }
        }
        return enviados;
    }
}
