package com.citas.notification;

import com.citas.model.Paciente;

/** Reto adicional: canal por el que se puede avisar o recordar una cita a un paciente. */
public interface Notificable {

    /** Nombre del canal ("Correo", "SMS", ...). */
    String canal();

    /** Envia el mensaje al paciente por este canal. */
    void notificar(Paciente destinatario, String mensaje);
}
