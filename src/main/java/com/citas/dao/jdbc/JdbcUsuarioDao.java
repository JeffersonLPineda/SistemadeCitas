package com.citas.dao.jdbc;

import com.citas.dao.DaoException;
import com.citas.dao.UsuarioDao;
import com.citas.exception.DatosInvalidosException;
import com.citas.exception.GestionCitasException;
import com.citas.model.EstadoRegistro;
import com.citas.model.Rol;
import com.citas.model.Usuario;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcUsuarioDao implements UsuarioDao {

    private static final String COLUMNAS = "id, nombre, correo, password_hash, rol, estado, codigo_asociado";
    private final Jdbc jdbc;

    public JdbcUsuarioDao(DataSource fuente) {
        this.jdbc = new Jdbc(fuente);
    }

    @Override
    public Usuario guardar(Usuario u) throws DatosInvalidosException {
        try {
            if (u.getId() == 0) {
                int id = jdbc.uno("INSERT INTO usuario (nombre, correo, password_hash, rol, estado, codigo_asociado) "
                                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
                        ps -> {
                            ps.setString(1, u.getNombre());
                            ps.setString(2, u.getCorreo());
                            ps.setString(3, u.getPasswordHash());
                            ps.setString(4, u.getRol().name());
                            ps.setString(5, u.getEstado().name());
                            ps.setString(6, u.getCodigoAsociado());
                        }, rs -> rs.getInt(1)).orElseThrow(() -> new DaoException("No se obtuvo el id del usuario", null));
                u.setId(id);
            } else {
                jdbc.ejecutar("UPDATE usuario SET password_hash = ?, estado = ? WHERE id = ?", ps -> {
                    ps.setString(1, u.getPasswordHash());
                    ps.setString(2, u.getEstado().name());
                    ps.setInt(3, u.getId());
                });
            }
            return u;
        } catch (DaoException e) {
            if (e.esViolacionUnica()) {
                throw new DatosInvalidosException("Ya existe un usuario con el correo " + u.getCorreo() + ".");
            }
            throw e;
        }
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        String c = correo.trim().toLowerCase();
        return jdbc.uno("SELECT " + COLUMNAS + " FROM usuario WHERE correo = ?", ps -> ps.setString(1, c), JdbcUsuarioDao::mapear);
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return jdbc.uno("SELECT " + COLUMNAS + " FROM usuario WHERE id = ?", ps -> ps.setInt(1, id), JdbcUsuarioDao::mapear);
    }

    @Override
    public List<Usuario> listar() {
        return jdbc.listar("SELECT " + COLUMNAS + " FROM usuario ORDER BY id", Jdbc.SIN_PARAMETROS, JdbcUsuarioDao::mapear);
    }

    private static Usuario mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        try {
            Usuario u = new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("correo"),
                    rs.getString("password_hash"), Rol.valueOf(rs.getString("rol")), rs.getString("codigo_asociado"));
            u.setEstado(EstadoRegistro.valueOf(rs.getString("estado")));
            return u;
        } catch (GestionCitasException e) {
            throw new DaoException("Usuario con datos inválidos en la base de datos", e);
        }
    }
}
