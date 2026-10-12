package com.citas.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee db.properties del classpath. Prioridad de cada valor: propiedad del sistema (-Ddb.url) >
 * variable de entorno (DB_URL) > archivo db.properties.
 */
public final class Configuracion {

    private final Properties props = new Properties();

    private Configuracion() {
    }

    public static Configuracion cargar() {
        Configuracion c = new Configuracion();
        try (InputStream in = Configuracion.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                c.props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
        return c;
    }

    private String valor(String clave, String entorno, String porDefecto) {
        String v = System.getProperty(clave);
        if (v == null || v.isEmpty()) {
            v = System.getenv(entorno);
        }
        if (v == null || v.isEmpty()) {
            v = props.getProperty(clave);
        }
        return v == null || v.isEmpty() ? porDefecto : v.trim();
    }

    /** "postgresql" (por defecto) o "memoria". */
    public String persistencia() {
        return valor("app.persistencia", "APP_PERSISTENCIA", "postgresql");
    }

    public boolean datosDemo() {
        return Boolean.parseBoolean(valor("app.datos-demo", "APP_DATOS_DEMO", "true"));
    }

    public String url() {
        return valor("db.url", "DB_URL", "jdbc:postgresql://localhost:5432/gestion_citas");
    }

    public String usuario() {
        return valor("db.user", "DB_USER", "citas_app");
    }

    public String password() {
        return valor("db.password", "DB_PASSWORD", "");
    }
}
