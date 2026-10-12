package com.citas.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Inicializa la base de datos al arrancar la aplicacion (falla pronto y con mensaje claro) y libera el pool al detenerla. */
@WebListener
public class AppListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Contexto.instancia();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo iniciar la aplicación. Revise la conexión a PostgreSQL "
                    + "(db.properties / DB_URL, DB_USER, DB_PASSWORD): " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Contexto.cerrar();
    }
}
