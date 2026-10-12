<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<h1>Seguimiento</h1>

<c:if test="${not empty cita}">
    <section class="tarjeta-info">
        <h2>Cita <c:out value="${cita.codigo}"/></h2>
        <p>
            <strong>Paciente:</strong> <c:out value="${cita.paciente.nombre}"/> ·
            <strong>Profesional:</strong> <c:out value="${cita.profesional.nombre}"/> ·
            <strong>Fecha:</strong> <c:out value="${cita.fecha}"/> <c:out value="${cita.hora}"/> ·
            <span class="estado estado-${cita.estado}"><c:out value="${cita.estado.etiqueta}"/></span>
        </p>
        <p><strong>Motivo:</strong> <c:out value="${cita.motivo}"/></p>
        <c:if test="${not cita.atendida}">
            <p class="nota">Solo una cita ATENDIDA puede recibir seguimiento.</p>
        </c:if>
    </section>

    <c:if test="${puedeRegistrar}">
        <section class="tarjeta-info">
            <h2>Registrar seguimiento</h2>
            <form action="${ctx}/seguimientos" method="post" class="formulario">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="accion" value="registrar">
                <input type="hidden" name="cita" value="<c:out value='${cita.codigo}'/>">

                <label for="observaciones">Observaciones *</label>
                <textarea id="observaciones" name="observaciones" rows="3" required maxlength="1000"><c:out value="${form.observaciones}"/></textarea>

                <label for="recomendaciones">Recomendaciones</label>
                <textarea id="recomendaciones" name="recomendaciones" rows="3" maxlength="1000"><c:out value="${form.recomendaciones}"/></textarea>

                <label for="proximaAccion">Próxima acción</label>
                <input type="text" id="proximaAccion" name="proximaAccion" maxlength="300" value="<c:out value='${form.proximaAccion}'/>">

                <div class="acciones">
                    <button type="submit" class="btn">Guardar seguimiento</button>
                    <a class="btn btn-sec" href="${ctx}/seguimientos">Volver</a>
                </div>
            </form>
        </section>
    </c:if>
</c:if>

<c:if test="${empty cita and not empty atendidas}">
    <section>
        <h2>Citas atendidas</h2>
        <div class="tabla-wrap">
            <table>
                <thead><tr><th>Código</th><th>Fecha</th><th>Paciente</th><th>Profesional</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="a" items="${atendidas}">
                    <tr>
                        <td><c:out value="${a.codigo}"/></td>
                        <td><c:out value="${a.fecha}"/></td>
                        <td><c:out value="${a.paciente.nombre}"/></td>
                        <td><c:out value="${a.profesional.nombre}"/></td>
                        <td><a class="btn btn-peq" href="${ctx}/seguimientos?cita=${a.codigo}">Ver / registrar</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
</c:if>

<section>
    <h2><c:choose><c:when test="${not empty cita}">Seguimientos de la cita</c:when><c:otherwise>Seguimientos registrados</c:otherwise></c:choose></h2>
    <c:choose>
        <c:when test="${empty seguimientos}"><p class="vacio">No hay seguimientos registrados.</p></c:when>
        <c:otherwise>
            <div class="tabla-wrap">
                <table>
                    <thead><tr><th>Código</th><th>Fecha</th><th>Cita</th><th>Paciente</th><th>Observaciones</th><th>Recomendaciones</th><th>Próxima acción</th></tr></thead>
                    <tbody>
                    <c:forEach var="s" items="${seguimientos}">
                        <tr>
                            <td><c:out value="${s.codigo}"/></td>
                            <td><c:out value="${s.fecha}"/></td>
                            <td><a href="${ctx}/seguimientos?cita=${s.cita.codigo}"><c:out value="${s.cita.codigo}"/></a></td>
                            <td><c:out value="${s.cita.paciente.nombre}"/></td>
                            <td><c:out value="${s.observaciones}"/></td>
                            <td><c:out value="${empty s.recomendaciones ? '—' : s.recomendaciones}"/></td>
                            <td><c:out value="${empty s.proximaAccion ? '—' : s.proximaAccion}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/pie.jspf" %>
