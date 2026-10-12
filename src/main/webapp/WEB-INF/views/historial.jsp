<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<h1>Historial</h1>

<c:if test="${not usuario.paciente}">
    <form action="${ctx}/historial" method="get" class="filtros">
        <label for="paciente">Paciente</label>
        <select id="paciente" name="paciente" required>
            <option value="">— Seleccione —</option>
            <c:forEach var="p" items="${opciones}">
                <option value="${p.codigo}" ${p.codigo == seleccionado ? 'selected' : ''}><c:out value="${p.codigo} · ${p.nombre}"/></option>
            </c:forEach>
        </select>
        <button type="submit" class="btn">Ver historial</button>
    </form>
</c:if>

<c:choose>
    <c:when test="${empty historial}">
        <c:if test="${not usuario.paciente}"><p class="vacio">Seleccione un paciente para ver su historial.</p></c:if>
    </c:when>
    <c:otherwise>
        <p class="subtitulo">Paciente: <strong><c:out value="${historial.paciente.nombre}"/></strong> (<c:out value="${historial.paciente.codigo}"/>)</p>
        <c:choose>
            <c:when test="${empty eventos}"><p class="vacio">Aún no hay citas ni seguimientos.</p></c:when>
            <c:otherwise>
                <div class="tabla-wrap">
                    <table>
                        <thead><tr><th>Fecha</th><th>Hora</th><th>Tipo</th><th>Código</th><th>Detalle</th><th>Estado</th></tr></thead>
                        <tbody>
                        <c:forEach var="e" items="${eventos}">
                            <tr>
                                <td><c:out value="${e.fecha}"/></td>
                                <td><c:out value="${e.horaTexto}"/></td>
                                <td><c:out value="${e.tipo.etiqueta}"/></td>
                                <td><c:out value="${e.codigo}"/></td>
                                <td><c:out value="${e.descripcion}"/></td>
                                <td><c:out value="${e.estado}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
<%@ include file="fragments/pie.jspf" %>
