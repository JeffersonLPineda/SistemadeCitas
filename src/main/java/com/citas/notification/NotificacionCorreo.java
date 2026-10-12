package com.citas.notification;

import com.citas.model.Paciente;

/** Canal de correo electronico (simulado: registra el envio en la bitacora). */
public class NotificacionCorreo implements Notificable {

    private final BitacoraNotificaciones bitacora;

    public NotificacionCorreo(BitacoraNotificaciones bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public String canal() {
        return "Correo";
    }

    @Override
    public void notificar(Paciente destinatario, String mensaje) {
        if (destinatario.getCorreo() == null || destinatario.getCorreo().isEmpty()) {
            return;
        }
        bitacora.registrar("[CORREO] Para: " + destinatario.getCorreo() + " | " + mensaje);
    }
}
