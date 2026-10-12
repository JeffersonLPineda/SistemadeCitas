# Sistema Web de Gestión de Citas y Seguimiento

Proyecto de **Programación II**: aplicación web Java (Servlets + JSP) con arquitectura en capas, POO, TDA, contratos, excepciones y pruebas JUnit.

## Requisitos
- JDK 11 o superior
- Apache Tomcat **9.x** (usa `javax.servlet`; para Tomcat 10+ habría que migrar a `jakarta.*`)
- **PostgreSQL 13 o superior** (los datos son persistentes)
- NetBeans (con soporte Maven) o Maven 3.6+

## Base de datos PostgreSQL
1. Cree la base y el usuario (con `psql` como administrador):
   ```sql
   CREATE USER citas_app WITH PASSWORD 'su_clave';
   CREATE DATABASE gestion_citas OWNER citas_app;
   ```
   Alternativa con Docker: `DB_PASSWORD=su_clave docker compose up -d` (usa `docker-compose.yml`).
2. Indique la conexión. Valores por defecto en `src/main/resources/db.properties`
   (`jdbc:postgresql://localhost:5432/gestion_citas`, usuario `citas_app`). Cada valor se puede
   sobrescribir con variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` o con propiedades de la JVM
   (`-Ddb.url=... -Ddb.user=... -Ddb.password=...`). En NetBeans: `Tools > Servers > Tomcat > Platform > VM Options`.
3. **No hace falta ejecutar scripts**: al iniciar, la aplicación crea las tablas si no existen
   (`src/main/resources/sql/schema.sql`) y, si la base está vacía, carga los usuarios y citas de demostración.
   Si PostgreSQL no responde, la aplicación no arranca y el log de Tomcat indica el motivo.

Tablas: `usuario`, `paciente`, `profesional`, `cita`, `seguimiento` (con claves foráneas y restricciones `CHECK`).
La regla *"un profesional no puede tener dos citas en la misma fecha y hora"* también está protegida en la base con un
índice único parcial (`ux_cita_profesional_horario`, no cuenta las citas canceladas), por lo que se cumple aunque haya
varias peticiones simultáneas. Para pruebas rápidas sin base de datos use `app.persistencia=memoria`.

## Abrir y ejecutar en NetBeans
1. `File > Open Project…` y seleccione la carpeta `gestion-citas` (proyecto Maven web).
2. Si NetBeans lo pide, registre Tomcat 9 en `Tools > Servers`.
3. Clic derecho sobre el proyecto > **Run**. Abre `http://localhost:8080/gestion-citas/`.
4. Pruebas: clic derecho > **Test** (o `mvn test`). Las pruebas contra PostgreSQL (`JdbcPersistenciaTest`) solo se
   ejecutan si define `TEST_DB_URL`, `TEST_DB_USER` y `TEST_DB_PASSWORD` apuntando a una base **de pruebas**
   (no la de la aplicación); si no, se omiten.

## Compilar por consola
```
mvn clean package        # genera target/gestion-citas.war y ejecuta JUnit
```
Copie el `.war` a la carpeta `webapps` de Tomcat 9 y entre a `http://localhost:8080/gestion-citas/`.

## Usuarios de demostración
| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@citas.local | Admin#2026 |
| Profesional | dra.lopez@citas.local / dr.perez@citas.local | Prof#2026 |
| Paciente | maria.gomez@citas.local / juan.ramirez@citas.local | Pac#2026 |

> En el código solo existen los **hashes** PBKDF2 de estas claves. Cambie las contraseñas desde *Usuarios* y **elimine esta tabla antes de la entrega final**.

Los datos se guardan en **PostgreSQL** y se conservan al reiniciar el servidor. Los servicios y servlets solo conocen las interfaces DAO; hay una implementación JDBC (`dao/jdbc`) y otra en memoria.

## Estructura
```
src/main/java/com/citas
  model/         Entidades: Usuario, Paciente, Profesional, Cita, Seguimiento, Disponibilidad, enums
  tda/           Agenda, ListaEnlazadaOrdenada, HistorialPaciente, EventoHistorial
  exception/     HorarioNoDisponible, CitaNoEncontrada, EstadoCitaInvalido, FechaInvalida, ...
  dao/           Interfaces DAO + implementación en memoria; dao/jdbc: implementación PostgreSQL (JDBC + PreparedStatement)
  service/       Reglas de negocio (CitaService, SeguimientoService, PoliticaAcceso, ...)
  controller/    Servlets (Login, Logout, Dashboard, Paciente, Profesional, Cita, Seguimiento, Historial, Usuario, AgendaApi)
  filter/        AutenticacionFilter (sesión obligatoria + token CSRF)
  notification/  Reto: Notificable, NotificacionCorreo, NotificacionSMS
  config/        Contexto (inyección manual), configuración, pool HikariCP, creación del esquema y datos iniciales
src/main/resources  db.properties y sql/schema.sql
src/main/webapp  JSP (en WEB-INF/views, inaccesibles directamente), CSS responsive
src/test/java    Pruebas JUnit 5
docs/            Diagramas UML y de arquitectura, documento de TDA y contratos
```

## Cumplimiento de la tarea
- **Módulos**: login, pacientes, profesionales, agenda de citas, seguimiento, historial y panel por rol.
- **Reglas de negocio**: fechas pasadas, doble reserva del profesional, cita cancelada no atendible, seguimiento solo de citas atendidas, paciente y profesional obligatorios, códigos únicos, pantallas protegidas.
- **Sesiones**: `HttpSession` guarda `usuario` y `rol`; `LogoutServlet` usa `session.invalidate()`; el filtro y cada vista comprueban la sesión.
- **Flujo**: Navegador → Servlet → Service → DAO → Datos → Servlet/JSP → Navegador (ver `docs/diagrama_arquitectura.png`).
- **Pruebas JUnit**: las 8 pedidas (`CitaServiceTest`, `SeguimientoServiceTest`) y adicionales para TDA, autenticación, contraseñas y persistencia en PostgreSQL.
- **Reto adicional**: `Notificable` con `NotificacionCorreo` y `NotificacionSMS` (simulados, quedan en una bitácora visible para el administrador), recordatorios y API REST `GET /api/agenda?profesional=PRO-0001&fecha=2026-10-12` (requiere sesión).
- **Seguridad extra**: contraseñas con PBKDF2 + sal, token CSRF en cada POST, salida HTML escapada (`c:out`), nueva sesión al iniciar sesión, autorización por rol y por pertenencia de los datos.

## Reglas de uso de la agenda
- Las citas se agendan en bloques de 30 minutos dentro de la disponibilidad del profesional.
- Flujo: `PROGRAMADA → CONFIRMADA → ATENDIDA`; `PROGRAMADA/CONFIRMADA → CANCELADA`.
- El profesional confirma y atiende; el paciente solicita y cancela; el administrador gestiona todo excepto atender.
