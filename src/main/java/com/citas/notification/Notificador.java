package com.citas.notification;

import com.citas.model.Paciente;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Envia un mensaje por todos los canales configurados; un fallo de notificacion nunca rompe la operacion de negocio. */
public class Notificador {

    private static final Logger LOG = Logger.getLogger(Notificador.class.getName());
    private final List<Notificable> canales;

    public Notificador(List<Notificable> canales) {
        this.canales = canales;
    }

    public void avisar(Paciente destinatario, String mensaje) {
        for (Notificable canal : canales) {
            try {
                canal.notificar(destinatario, mensaje);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Falló la notificación por " + canal.canal(), e);
            }
        }
    }
}
