package com.citas.notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

/** Registro de los ultimos avisos "enviados" (los canales son simulados: no se conectan a un proveedor real). */
public class BitacoraNotificaciones {

    private static final Logger LOG = Logger.getLogger(BitacoraNotificaciones.class.getName());
    private static final int MAXIMO = 50;

    private final Deque<String> registros = new ConcurrentLinkedDeque<>();

    public void registrar(String texto) {
        String linea = LocalDateTime.now().withNano(0) + "  " + texto;
        registros.addFirst(linea);
        while (registros.size() > MAXIMO) {
            registros.pollLast();
        }
        LOG.info(linea);
    }

    /** Los {@code cantidad} registros mas recientes (el primero es el mas nuevo). */
    public List<String> ultimas(int cantidad) {
        List<String> r = new ArrayList<>(registros);
        return Collections.unmodifiableList(r.subList(0, Math.min(cantidad, r.size())));
    }
}
