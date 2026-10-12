package com.citas.tda;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * TDA Lista Enlazada Ordenada (implementacion propia con nodos).
 *
 * <p><b>Modelo abstracto:</b> secuencia de elementos ordenada segun un {@link Comparator}.<br>
 * <b>Operaciones:</b> insertar, eliminar, primero, tamano, estaVacia, iterator, aLista.<br>
 * <b>Invariantes:</b> para todo par de nodos consecutivos a, b: orden.compare(a, b) &lt;= 0;
 * {@code tamano} coincide con la cantidad de nodos; no admite elementos nulos.</p>
 */
public class ListaEnlazadaOrdenada<T> implements Iterable<T> {

    private static final class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private final Comparator<? super T> orden;
    private Nodo<T> cabeza;
    private int tamano;

    public ListaEnlazadaOrdenada(Comparator<? super T> orden) {
        this.orden = Objects.requireNonNull(orden, "El criterio de orden es obligatorio");
    }

    /**
     * Precondicion: elemento != null.
     * Postcondicion: tamano aumenta en 1 y la lista sigue ordenada; ante empates el nuevo queda despues.
     */
    public void insertar(T elemento) {
        Objects.requireNonNull(elemento, "No se admiten elementos nulos");
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null || orden.compare(elemento, cabeza.dato) < 0) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null && orden.compare(elemento, actual.siguiente.dato) >= 0) {
                actual = actual.siguiente;
            }
            nuevo.siguiente = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamano++;
    }

    /** Elimina la primera aparicion (segun equals). Devuelve true si se elimino algo. */
    public boolean eliminar(T elemento) {
        Nodo<T> anterior = null;
        for (Nodo<T> n = cabeza; n != null; anterior = n, n = n.siguiente) {
            if (n.dato.equals(elemento)) {
                if (anterior == null) {
                    cabeza = n.siguiente;
                } else {
                    anterior.siguiente = n.siguiente;
                }
                tamano--;
                return true;
            }
        }
        return false;
    }

    /** @throws NoSuchElementException si la lista esta vacia. */
    public T primero() {
        if (cabeza == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return cabeza.dato;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public List<T> aLista() {
        List<T> copia = new ArrayList<>(tamano);
        for (T t : this) {
            copia.add(t);
        }
        return copia;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}
