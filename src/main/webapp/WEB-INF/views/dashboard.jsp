<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<h1>Panel principal</h1>
<p class="subtitulo">Bienvenido(a), <strong><c:out value="${usuario.nombre}"/></strong> — <c:out value="${usuario.rol.etiqueta}"/></p>

<section class="tarjetas" aria-label="Indicadores">
    <c:forEach var="i" items="${indicadores}">
        <div class="tarjeta">
            <span class="valor"><c:out value="${i.value}"/></span>
            <span class="etq"><c:out value="${i.key}"/></span>
        </div>
    </c:forEach>
</section>

<section>
    <h2>Accesos rápidos</h2>
    <div class="acciones">
        <c:if test="${usuario.admin or usuario.paciente}">
            <a class="btn" href="${ctx}/citas?accion=nueva">Nueva cita</a>
        </c:if>
        <a class="btn btn-sec" href="${ctx}/citas">Ver citas</a>
        <a class="btn btn-sec" href="${ctx}/historial">Historial</a>
        <c:if test="${usuario.admin}">
            <a class="btn btn-sec" href="${ctx}/pacientes?accion=nuevo">Registrar paciente</a>
            <a class="btn btn-sec" href="${ctx}/profesionales?accion=nuevo">Registrar profesional</a>
        </c:if>
    </div>
</section>

<section>
    <h2>Próximas citas</h2>
    <c:choose>
        <c:when test="${empty proximas}">
            <p class="vacio">No hay citas próximas.</p>
        </c:when>
        <c:otherwise>
            <div class="tabla-wrap">
                <table>
                    <thead><tr><th>Código</th><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Profesional</th><th>Estado</th></tr></thead>
                    <tbody>
                    <c:forEach var="c" items="${proximas}">
                        <tr>
                            <td><c:out value="${c.codigo}"/></td>
                            <td><c:out value="${c.fecha}"/></td>
                            <td><c:out value="${c.hora}"/></td>
                            <td><c:out value="${c.paciente.nombre}"/></td>
                            <td><c:out value="${c.profesional.nombre}"/></td>
                            <td><span class="estado estado-${c.estado}"><c:out value="${c.estado.etiqueta}"/></span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<c:if test="${usuario.admin}">
    <section>
        <h2>Recordatorios</h2>
        <form action="${ctx}/citas" method="post" class="en-linea">
            <input type="hidden" name="csrf" value="${csrf}">
            <input type="hidden" name="accion" value="recordatorios">
            <button type="submit" class="btn btn-sec">Enviar recordatorios de las citas de mañana</button>
        </form>
        <p class="nota">Los canales de correo y SMS son simulados: cada envío queda en esta bitácora.</p>
        <c:choose>
            <c:when test="${empty bitacora}"><p class="vacio">Aún no hay notificaciones.</p></c:when>
            <c:otherwise>
                <ul class="bitacora">
                    <c:forEach var="linea" items="${bitacora}"><li><c:out value="${linea}"/></li></c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </section>
</c:if>
<%@ include file="fragments/pie.jspf" %>
