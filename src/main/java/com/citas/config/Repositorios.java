package com.citas.config;

import com.citas.dao.CitaDao;
import com.citas.dao.MemoriaCitaDao;
import com.citas.dao.MemoriaPacienteDao;
import com.citas.dao.MemoriaProfesionalDao;
import com.citas.dao.MemoriaSeguimientoDao;
import com.citas.dao.MemoriaUsuarioDao;
import com.citas.dao.PacienteDao;
import com.citas.dao.ProfesionalDao;
import com.citas.dao.SeguimientoDao;
import com.citas.dao.UsuarioDao;
import com.citas.dao.jdbc.JdbcCitaDao;
import com.citas.dao.jdbc.JdbcPacienteDao;
import com.citas.dao.jdbc.JdbcProfesionalDao;
import com.citas.dao.jdbc.JdbcSeguimientoDao;
import com.citas.dao.jdbc.JdbcUsuarioDao;
import javax.sql.DataSource;

/** Conjunto de DAO a usar: PostgreSQL (persistente) o memoria (pruebas rapidas). */
public final class Repositorios {

    private final UsuarioDao usuarios;
    private final PacienteDao pacientes;
    private final ProfesionalDao profesionales;
    private final CitaDao citas;
    private final SeguimientoDao seguimientos;

    private Repositorios(UsuarioDao u, PacienteDao p, ProfesionalDao pr, CitaDao c, SeguimientoDao s) {
        this.usuarios = u;
        this.pacientes = p;
        this.profesionales = pr;
        this.citas = c;
        this.seguimientos = s;
    }

    public static Repositorios enMemoria() {
        return new Repositorios(new MemoriaUsuarioDao(), new MemoriaPacienteDao(), new MemoriaProfesionalDao(),
                new MemoriaCitaDao(), new MemoriaSeguimientoDao());
    }

    public static Repositorios jdbc(DataSource fuente) {
        return new Repositorios(new JdbcUsuarioDao(fuente), new JdbcPacienteDao(fuente), new JdbcProfesionalDao(fuente),
                new JdbcCitaDao(fuente), new JdbcSeguimientoDao(fuente));
    }

    public UsuarioDao usuarios() { return usuarios; }
    public PacienteDao pacientes() { return pacientes; }
    public ProfesionalDao profesionales() { return profesionales; }
    public CitaDao citas() { return citas; }
    public SeguimientoDao seguimientos() { return seguimientos; }
}
