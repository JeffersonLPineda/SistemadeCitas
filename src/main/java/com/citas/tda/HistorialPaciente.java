package com.citas.tda;

import com.citas.model.Cita;
import com.citas.model.Paciente;
import com.citas.model.Seguimiento;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * TDA Historial: linea de tiempo cronologica (citas y seguimientos) de un paciente.
 *
 * <p><b>Operaciones:</b> agregarCita, agregarSeguimiento, eventos, tamano.<br>
 * <b>Invariantes:</b> los eventos siempre estan en orden cronologico ascendente;
 * todos los eventos pertenecen al paciente del historial.</p>
 * Se apoya en {@link ListaEnlazadaOrdenada}.
 */
public class HistorialPaciente {

    private final Paciente paciente;
    private final ListaEnlazadaOrdenada<EventoHistorial> eventos = new ListaEnlazadaOrdenada<>(
            Comparator.comparing(EventoHistorial::momentoOrden)
                      .thenComparing(EventoHistorial::getTipo));

    public HistorialPaciente(Paciente paciente) {
        this.paciente = Objects.requireNonNull(paciente, "El paciente es obligatorio");
    }

    /** Precondicion: la cita pertenece al paciente del historial. */
    public void agregarCita(Cita cita) {
        verificarPropietario(cita.getPaciente());
        eventos.insertar(new EventoHistorial(EventoHistorial.Tipo.CITA, cita.getCodigo(), cita.getFecha(),
                cita.getHora(), cita.getMotivo() + " (con " + cita.getProfesional().getNombre() + ")",
                cita.getEstado().getEtiqueta()));
    }

    /** Precondicion: el seguimiento corresponde a una cita del paciente. */
    public void agregarSeguimiento(Seguimiento s) {
        verificarPropietario(s.getCita().getPaciente());
        String texto = s.getObservaciones()
                + (s.getRecomendaciones().isEmpty() ? "" : " | Recomendaciones: " + s.getRecomendaciones())
                + (s.getProximaAccion().isEmpty() ? "" : " | Próxima acción: " + s.getProximaAccion());
        eventos.insertar(new EventoHistorial(EventoHistorial.Tipo.SEGUIMIENTO, s.getCodigo(), s.getFecha(),
                null, texto, "Cita " + s.getCita().getCodigo()));
    }

    private void verificarPropietario(Paciente otro) {
        if (!paciente.getCodigo().equals(otro.getCodigo())) {
            throw new IllegalArgumentException("El evento no pertenece al paciente " + paciente.getCodigo());
        }
    }

    public Paciente getPaciente() { return paciente; }
    public List<EventoHistorial> getEventos() { return eventos.aLista(); }
    public int tamano() { return eventos.tamano(); }
}
