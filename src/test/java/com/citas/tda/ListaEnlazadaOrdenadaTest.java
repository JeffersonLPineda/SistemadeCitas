package com.citas.tda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class ListaEnlazadaOrdenadaTest {

    @Test
    void mantieneElOrdenAlInsertar() {
        ListaEnlazadaOrdenada<Integer> lista = new ListaEnlazadaOrdenada<>(Comparator.naturalOrder());
        for (int n : new int[]{5, 1, 4, 2, 3}) {
            lista.insertar(n);
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), lista.aLista());
        assertEquals(5, lista.tamano());
        assertEquals(1, lista.primero());
    }

    @Test
    void eliminaElementosYActualizaElTamano() {
        ListaEnlazadaOrdenada<Integer> lista = new ListaEnlazadaOrdenada<>(Comparator.naturalOrder());
        lista.insertar(1);
        lista.insertar(2);
        lista.insertar(3);
        assertTrue(lista.eliminar(2));
        assertFalse(lista.eliminar(99));
        assertTrue(lista.eliminar(1)); // cabeza
        assertEquals(Arrays.asList(3), lista.aLista());
        assertEquals(1, lista.tamano());
    }

    @Test
    void listaVaciaYElementosNulos() {
        ListaEnlazadaOrdenada<String> lista = new ListaEnlazadaOrdenada<>(Comparator.naturalOrder());
        assertTrue(lista.estaVacia());
        assertThrows(NoSuchElementException.class, lista::primero);
        assertThrows(NullPointerException.class, () -> lista.insertar(null));
    }

    @Test
    void losEmpatesConservanElOrdenDeLlegada() {
        ListaEnlazadaOrdenada<String> lista = new ListaEnlazadaOrdenada<>(Comparator.comparing(String::length));
        lista.insertar("bb");
        lista.insertar("aa");
        lista.insertar("c");
        assertEquals(Arrays.asList("c", "bb", "aa"), lista.aLista());
    }
}
