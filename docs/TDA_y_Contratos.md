# Documento de TDA y Contratos de clase

Proyecto: **Sistema Web de Gestión de Citas y Seguimiento** · Programación II

## 1. Tipos de Datos Abstractos (TDA)

### 1.1 `ListaEnlazadaOrdenada<T>` (paquete `com.citas.tda`)
Implementación propia con nodos enlazados.

| Elemento | Descripción |
|---|---|
| Modelo abstracto | Secuencia de elementos ordenada según un `Comparator`. |
| Operaciones | `insertar`, `eliminar`, `primero`, `tamano`, `estaVacia`, `iterator`, `aLista` |
| Invariantes | Para nodos consecutivos a, b: `orden.compare(a, b) <= 0`; `tamano` = número de nodos; no admite `null`. |
| Uso | Base del historial cronológico. |

### 1.2 `Agenda` (paquete `com.citas.tda`)
Citas vigentes de **un** profesional, indexadas por fecha y hora (`TreeMap<LocalDateTime, Cita>`).

| Elemento | Descripción |
|---|---|
| Modelo abstracto | Función parcial (fecha, hora) → cita. |
| Operaciones | `insertar`, `estaOcupado`, `obtener`, `liberar`, `citasDelDia`, `todas`, `tamano`, `estaVacia`, `Agenda.desde(...)` |
| Invariantes | No hay dos citas con la misma fecha y hora; solo contiene citas no canceladas; siempre ordenada cronológicamente. |
| Uso | `CitaService.programarCita` arma la agenda del profesional y llama a `insertar`: si el horario está ocupado lanza `HorarioNoDisponibleException`. |

### 1.3 `HistorialPaciente` (paquete `com.citas.tda`)
Línea de tiempo (citas y seguimientos) de un paciente, apoyada en `ListaEnlazadaOrdenada<EventoHistorial>`.

| Elemento | Descripción |
|---|---|
| Operaciones | `agregarCita`, `agregarSeguimiento`, `getEventos`, `tamano` |
| Invariantes | Eventos en orden cronológico ascendente; todos pertenecen al paciente del historial. |

### 1.4 Objeto-valor `Disponibilidad`
Inmutable. Invariantes: al menos un día; `inicio < fin`. Operación clave: `permite(fecha, hora)`.

## 2. Contratos de clase (precondiciones, postcondiciones, invariantes, excepciones)

### 2.1 `CitaService.programarCita(codigoPaciente, codigoProfesional, fecha, hora, motivo)`
| | |
|---|---|
| Precondiciones | Paciente y profesional existen y están activos; fecha no anterior a hoy (si es hoy, la hora no ha pasado); hora dentro de la disponibilidad y en bloques de 30 min; horario libre; motivo no vacío. |
| Postcondición | Se registra una cita con código único y estado `PROGRAMADA`; se notifica al paciente. |
| Invariantes | La cita mantiene paciente, profesional y un estado válido; el profesional no tiene dos citas vigentes en la misma fecha y hora. |
| Excepciones | `FechaInvalidaException`, `HorarioNoDisponibleException`, `DatosInvalidosException` |

### 2.2 `CitaService.cancelarCita(codigo)` / `Cita.cancelar()`
| | |
|---|---|
| Precondiciones | La cita existe y está `PROGRAMADA` o `CONFIRMADA`. |
| Postcondición | Estado `CANCELADA`; el horario del profesional queda libre. |
| Invariante | Una cita `ATENDIDA` o `CANCELADA` no cambia de estado. |
| Excepciones | `CitaNoEncontradaException`, `EstadoCitaInvalidoException` |

### 2.3 `CitaService.atenderCita(codigo)` / `Cita.atender()`
| | |
|---|---|
| Precondiciones | La cita existe y está `CONFIRMADA` (una cita `CANCELADA` nunca puede atenderse). |
| Postcondición | Estado `ATENDIDA`. |
| Excepciones | `CitaNoEncontradaException`, `EstadoCitaInvalidoException` |

### 2.4 `SeguimientoService.registrar(codigoCita, observaciones, recomendaciones, proximaAccion)` / constructor de `Seguimiento`
| | |
|---|---|
| Precondiciones | La cita existe y está `ATENDIDA`; observaciones no vacías. |
| Postcondición | Seguimiento guardado con la fecha de hoy y asociado a la cita. |
| Invariante | Solo existen seguimientos de citas atendidas. |
| Excepciones | `CitaNoEncontradaException`, `EstadoCitaInvalidoException`, `DatosInvalidosException` |

### 2.5 `Agenda.insertar(cita)`
| | |
|---|---|
| Precondiciones | `cita != null`, `cita.ocupaHorario()`, horario libre. |
| Postcondición | La cita queda en la agenda; `tamano()` aumenta en 1. |
| Excepción | `HorarioNoDisponibleException` |

### 2.6 `AutenticacionService.autenticar(correo, password)`
| | |
|---|---|
| Precondición | Correo y contraseña no nulos. |
| Postcondición | Devuelve el usuario activo cuyas credenciales coinciden (comparación contra hash PBKDF2). |
| Excepción | `AutenticacionException` (mensaje genérico: no revela si el correo existe) |

## 3. Máquina de estados de la cita

```
PROGRAMADA ──confirmar──► CONFIRMADA ──atender──► ATENDIDA
    │                         │
    └──────cancelar───────────┴──────► CANCELADA
```
Implementada en `EstadoCita.puedePasarA(...)`.

## 4. Excepciones del sistema
`HorarioNoDisponibleException`, `CitaNoEncontradaException`, `EstadoCitaInvalidoException`, `FechaInvalidaException` (las cuatro exigidas) más `DatosInvalidosException`, `AutenticacionException` y `AccesoDenegadoException`. Todas heredan de `GestionCitasException`. Los servlets las convierten en mensajes claros para el usuario (nunca se usa solo `System.out.println`).
