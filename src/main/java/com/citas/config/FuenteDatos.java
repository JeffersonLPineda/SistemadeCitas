package com.citas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** Crea el pool de conexiones (HikariCP) hacia PostgreSQL. */
public final class FuenteDatos {

    private FuenteDatos() {
    }

    public static HikariDataSource crear(String url, String usuario, String password) {
        HikariConfig cfg = new HikariConfig();
        cfg.setPoolName("gestion-citas");
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setJdbcUrl(url);
        cfg.setUsername(usuario);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(10_000); // falla rapido y con mensaje claro si la BD no responde
        return new HikariDataSource(cfg);
    }

    public static HikariDataSource crear(Configuracion c) {
        return crear(c.url(), c.usuario(), c.password());
    }
}
