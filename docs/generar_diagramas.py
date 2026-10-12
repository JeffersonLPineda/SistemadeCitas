"""Genera los diagramas PNG (arquitectura y clases UML). Uso: python3 generar_diagramas.py"""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle

def caja(ax, x, y, w, h, titulo, lineas=(), color="#eaf0f7", fs=8):
    ax.add_patch(Rectangle((x, y), w, h, fc=color, ec="#1f5fa8", lw=1.2))
    th = 0.55
    ax.add_patch(Rectangle((x, y + h - th), w, th, fc="#cfe0f5", ec="#1f5fa8", lw=1.2))
    ax.text(x + w / 2, y + h - th / 2, titulo, ha="center", va="center", fontsize=fs + 1, fontweight="bold")
    for i, l in enumerate(lineas):
        ax.text(x + 0.12, y + h - th - 0.32 - i * 0.36, l, ha="left", va="center", fontsize=fs - 0.5, family="monospace")

def flecha(ax, p, q, txt="", estilo="->", off=(0, 0.15)):
    ax.annotate("", xy=q, xytext=p, arrowprops=dict(arrowstyle=estilo, color="#333", lw=1.1))
    if txt:
        ax.text((p[0] + q[0]) / 2 + off[0], (p[1] + q[1]) / 2 + off[1], txt, fontsize=7, ha="center", color="#333")

# ---------------- Arquitectura ----------------
fig, ax = plt.subplots(figsize=(11, 6.2)); ax.set_xlim(0, 22); ax.set_ylim(0, 12.5); ax.axis("off")
ax.text(11, 12.0, "Arquitectura web en capas (MVC) - Gestión de Citas y Seguimiento", ha="center", fontsize=13, fontweight="bold")
capas = [
    ("Navegador", "HTML / CSS / JSP\n(login, dashboard, citas,\npacientes, historial...)", "#fff4d6"),
    ("Controlador", "Servlets + AutenticacionFilter\nLogin, Logout, Cita, Paciente,\nProfesional, Seguimiento...", "#e0efff"),
    ("Servicio", "Clases Service\nCitaService, SeguimientoService,\nPoliticaAcceso (reglas de negocio)", "#e6f4ea"),
    ("Datos", "DAO / Repository (JDBC)\nJdbcCitaDao, JdbcUsuarioDao...\nPool HikariCP -> PostgreSQL", "#f3e5f5"),
]
xs = [0.5, 6.1, 11.7, 17.3]
for (t, d, c), x in zip(capas, xs):
    ax.add_patch(FancyBboxPatch((x, 5.0), 4.3, 4.8, boxstyle="round,pad=0.05", fc=c, ec="#1f5fa8", lw=1.5))
    ax.text(x + 2.15, 9.2, t, ha="center", fontsize=11, fontweight="bold")
    ax.text(x + 2.15, 7.2, d, ha="center", va="center", fontsize=8.5)
for i in range(3):
    flecha(ax, (xs[i] + 4.4, 8.0), (xs[i + 1] - 0.1, 8.0), "petición" if i == 0 else "llama a")
    flecha(ax, (xs[i + 1] - 0.1, 6.3), (xs[i] + 4.4, 6.3), "respuesta" if i == 0 else "resultado", off=(0, -0.45))
ax.add_patch(FancyBboxPatch((6.1, 1.0), 15.5, 2.6, boxstyle="round,pad=0.05", fc="#fdeceb", ec="#b3261e", lw=1.5))
ax.text(13.85, 3.0, "Dominio (transversal a Servicio y Datos)", ha="center", fontsize=10, fontweight="bold")
ax.text(13.85, 2.0, "model: Usuario, Paciente, Profesional, Cita, Seguimiento, Disponibilidad\n"
        "tda: Agenda, ListaEnlazadaOrdenada, HistorialPaciente   |   exception: reglas de negocio con contratos",
        ha="center", va="center", fontsize=8.5)
flecha(ax, (13.8, 5.0), (13.8, 3.7), estilo="<->")
ax.text(0.5, 3.6, "Flujo completo:\nNAVEGADOR -> SERVLET -> SERVICIO -> DAO -> DATOS\n-> SERVLET -> JSP (vista) -> NAVEGADOR", fontsize=9, va="top",
        bbox=dict(boxstyle="round", fc="white", ec="#999"))
ax.text(0.5, 0.5, "Sesión: HttpSession guarda usuario y rol; el filtro bloquea pantallas protegidas y valida el token CSRF.", fontsize=8, color="#444")
fig.savefig("diagrama_arquitectura.png", dpi=160, bbox_inches="tight"); plt.close(fig)

# ---------------- Clases UML ----------------
fig, ax = plt.subplots(figsize=(17, 10.5)); ax.set_xlim(0, 40); ax.set_ylim(0, 24); ax.axis("off")
ax.text(20, 23.3, "Diagrama de clases UML (simplificado) - Gestión de Citas y Seguimiento", ha="center", fontsize=14, fontweight="bold")

caja(ax, 1, 17.5, 7.0, 5.2, "Usuario", ["- id: int", "- nombre, correo", "- passwordHash (PBKDF2)", "- rol: ADMINISTRADOR |", "         PROFESIONAL | PACIENTE", "- estado, codigoAsociado"])
caja(ax, 1, 10, 7.0, 5.0, "Paciente", ["- codigo: String", "- nombre, telefono", "- correo, direccion", "- estado", "+ actualizarDatos()"])
caja(ax, 11, 10, 7.2, 6.0, "Cita", ["- codigo: String", "- paciente: Paciente", "- profesional: Profesional", "- fecha, hora, motivo", "- estado: EstadoCita", "+ confirmar() / cancelar()", "+ atender()"], color="#fff4d6")
caja(ax, 21, 10, 7.0, 5.0, "Profesional", ["- codigo, nombre", "- especialidad", "- disponibilidad", "- estado", "+ actualizarDatos()"])
caja(ax, 1, 2, 7.0, 5.4, "Seguimiento", ["- codigo: String", "- cita: Cita (ATENDIDA)", "- fecha", "- observaciones", "- recomendaciones", "- proximaAccion"])
caja(ax, 11, 2, 7.2, 5.6, "«enum» EstadoCita", ["PROGRAMADA", "CONFIRMADA", "ATENDIDA", "CANCELADA", "+ puedePasarA(destino)"], color="#f7f7f7")
caja(ax, 21, 2, 7.0, 5.4, "Disponibilidad", ["- dias: Set<DayOfWeek>", "- inicio, fin: LocalTime", "+ permite(fecha, hora)", "+ incluyeDia(nombre)"], color="#f7f7f7")
caja(ax, 11, 18, 7.2, 4.8, "CitaService", ["+ programarCita()", "+ confirmarCita()", "+ cancelarCita()", "+ atenderCita()", "+ agendaDe(profesional)"], color="#e0efff")
caja(ax, 31, 17.5, 8, 5.2, "Agenda  «TDA»", ["- citas: TreeMap<fecha-hora,Cita>", "+ insertar(cita)", "+ estaOcupado(f, h)", "+ liberar(cita)", "+ citasDelDia(f)"], color="#e6f4ea")
caja(ax, 31, 10, 8, 5.0, "HistorialPaciente  «TDA»", ["- paciente: Paciente", "- eventos: ListaEnlazada", "+ agregarCita(c)", "+ agregarSeguimiento(s)", "+ getEventos()"], color="#e6f4ea")
caja(ax, 31, 2.5, 8, 5.0, "ListaEnlazadaOrdenada<T>", ["- cabeza: Nodo<T>", "+ insertar(e)", "+ eliminar(e)", "+ primero()", "+ aLista()"], color="#e6f4ea")

def rel(p, q, t="", e="->", off=(0, 0.25)): flecha(ax, p, q, t, e, off)
rel((11, 13), (8, 13), "paciente (1)")
rel((18.2, 13), (21, 13), "profesional (1)")
rel((4.5, 17.5), (4.5, 15), "codigoAsociado", off=(1.6, 0))
rel((14.6, 18), (14.6, 16), "gestiona", off=(1.0, 0))
rel((14.6, 10), (14.6, 7.6), "estado", off=(0.9, 0))
rel((24.5, 10), (24.5, 7.4), "tiene", off=(0.8, 0))
rel((8, 6.5), (11.5, 10), "1..* sobre cita", off=(-1.6, -0.2))
rel((18.2, 21), (31, 21), "construye y usa")
rel((35, 10), (35, 7.5), "usa")
ax.text(1, 0.6, "Paquetes: model (entidades) · tda (Agenda, HistorialPaciente, ListaEnlazadaOrdenada) · service (reglas de negocio) · dao (interfaces + JDBC/PostgreSQL + memoria)\n"
        "controller (Servlets) · filter · exception (HorarioNoDisponible, CitaNoEncontrada, EstadoCitaInvalido, FechaInvalida...) · notification (Notificable, NotificacionCorreo, NotificacionSMS)",
        fontsize=8.5, color="#444", va="center")
fig.savefig("diagrama_clases.png", dpi=150, bbox_inches="tight"); plt.close(fig)
print("ok")
