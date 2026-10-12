package com.citas.notification;

import com.citas.model.Paciente;

/** Canal SMS (simulado: registra el envio en la bitacora). Solo se envia si el paciente tiene telefono. */
public class NotificacionSMS implements Notificable {

    private final BitacoraNotificaciones bitacora;

    public NotificacionSMS(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public String canal() {
        return "SMS";
    }

    @Override
    public void notificar(Paciente destinatario, String mensaje) {
        if (destinatario.getTelefono() == null || destinatario.getTelefono().isEmpty()) {
            return;
        }
        bitacora.registrar("[SMS] Al: " + destinatario.getTelefono() + " | " + mensaje);
    }
}
