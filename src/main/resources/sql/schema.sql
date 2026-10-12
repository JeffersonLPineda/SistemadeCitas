-- Esquema PostgreSQL de "Gestion de Citas y Seguimiento".
-- Es idempotente: la aplicacion lo ejecuta al iniciar (CREATE ... IF NOT EXISTS).

CREATE SEQUENCE IF NOT EXISTS seq_paciente;
CREATE SEQUENCE IF NOT EXISTS seq_profesional;
CREATE SEQUENCE IF NOT EXISTS seq_cita;
CREATE SEQUENCE IF NOT EXISTS seq_seguimiento;

CREATE TABLE IF NOT EXISTS usuario (
    id               SERIAL PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    correo           VARCHAR(100) NOT NULL UNIQUE,
    password_hash    VARCHAR(200) NOT NULL,
    rol              VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'PROFESIONAL', 'PACIENTE')),
    estado           VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    codigo_asociado  VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS paciente (
    codigo     VARCHAR(20)  PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    telefono   VARCHAR(20)  NOT NULL DEFAULT '',
    correo     VARCHAR(100) NOT NULL,
    direccion  VARCHAR(200) NOT NULL DEFAULT '',
    estado     VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE IF NOT EXISTS profesional (
    codigo        VARCHAR(20)  PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    especialidad  VARCHAR(100) NOT NULL,
    dias          VARCHAR(80)  NOT NULL,
    hora_inicio   TIME         NOT NULL,
    hora_fin      TIME         NOT NULL,
    estado        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CHECK (hora_inicio < hora_fin)
);

CREATE TABLE IF NOT EXISTS cita (
    codigo              VARCHAR(20)  PRIMARY KEY,
    codigo_paciente     VARCHAR(20)  NOT NULL REFERENCES paciente (codigo),
    codigo_profesional  VARCHAR(20)  NOT NULL REFERENCES profesional (codigo),
    fecha               DATE         NOT NULL,
    hora                TIME         NOT NULL,
    motivo              VARCHAR(300) NOT NULL,
    estado              VARCHAR(12)  NOT NULL CHECK (estado IN ('PROGRAMADA', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA'))
);

-- Regla de negocio protegida tambien en la BD: un profesional no puede tener dos citas
-- vigentes (no canceladas) en la misma fecha y hora.
CREATE UNIQUE INDEX IF NOT EXISTS ux_cita_profesional_horario
    ON cita (codigo_profesional, fecha, hora) WHERE estado <> 'CANCELADA';
CREATE INDEX IF NOT EXISTS ix_cita_paciente ON cita (codigo_paciente);

CREATE TABLE IF NOT EXISTS seguimiento (
    codigo           VARCHAR(20)   PRIMARY KEY,
    codigo_cita      VARCHAR(20)   NOT NULL REFERENCES cita (codigo),
    fecha            DATE          NOT NULL,
    observaciones    VARCHAR(1000) NOT NULL,
    recomendaciones  VARCHAR(1000) NOT NULL DEFAULT '',
    proxima_accion   VARCHAR(300)  NOT NULL DEFAULT ''
);
CREATE INDEX IF NOT EXISTS ix_seguimiento_cita ON seguimiento (codigo_cita);
