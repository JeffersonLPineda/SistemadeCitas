package com.citas.tda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Elemento del historial: una cita o un seguimiento (objeto-valor inmutable). */
public final class EventoHistorial {

    public enum Tipo {
        CITA("Cita"), SEGUIMIENTO("Seguimiento");

        private final String etiqueta;

        Tipo(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    private final Tipo tipo;
    private final String codigo;
    private final LocalDate fecha;
    private final LocalTime hora; // nulo para seguimientos
    private final String descripcion;
    private final String estado;

    public EventoHistorial(Tipo tipo, String codigo, LocalDate fecha, LocalTime hora, String descripcion, String estado) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.fecha = fecha;
        this.hora = hora;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    /** Momento usado para ordenar: un seguimiento va despues de las citas del mismo dia. */
    public LocalDateTime momentoOrden() {
        return LocalDateTime.of(fecha, hora != null ? hora : LocalTime.MAX);
    }

    public Tipo getTipo() { return tipo; }
    public String getCodigo() { return codigo; }
    public LocalDate getFecha() { return fecha; }
    public String getHoraTexto() { return hora == null ? "—" : hora.toString(); }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
}
