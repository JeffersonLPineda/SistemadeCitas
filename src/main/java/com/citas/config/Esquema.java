package com.citas.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** Crea las tablas ejecutando /sql/schema.sql (idempotente: CREATE ... IF NOT EXISTS). */
public final class Esquema {

    private Esquema() {
    }

    public static void crear(DataSource fuente) {
        List<String> sentencias = leerSentencias();
        try (Connection c = fuente.getConnection()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                for (String sql : sentencias) {
                    st.execute(sql);
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear el esquema de la base de datos: " + e.getMessage(), e);
        }
    }

    private static List<String> leerSentencias() {
        try (InputStream in = Esquema.class.getResourceAsStream("/sql/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró /sql/schema.sql en el classpath");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = r.readLine()) != null) {
                    if (!linea.trim().startsWith("--")) {
                        sb.append(linea).append('\n');
                    }
                }
            }
            List<String> sentencias = new ArrayList<>();
            for (String s : sb.toString().split(";")) {
                if (!s.trim().isEmpty()) {
                    sentencias.add(s.trim());
                }
            }
            return sentencias;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer schema.sql", e);
        }
    }
}
