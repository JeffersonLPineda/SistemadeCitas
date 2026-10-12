<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<div class="encabezado">
    <h1>Agenda de citas</h1>
    <c:if test="${usuario.admin or usuario.paciente}">
        <a class="btn" href="${ctx}/citas?accion=nueva">Nueva cita</a>
    </c:if>
</div>

<form action="${ctx}/citas" method="get" class="filtros">
    <label for="estado">Estado</label>
    <select id="estado" name="estado">
        <option value="">Todos</option>
        <c:forEach var="e" items="${estados}">
            <option value="${e}" ${e.name() == filtroEstado ? 'selected' : ''}><c:out value="${e.etiqueta}"/></option>
        </c:forEach>
    </select>
    <label for="fecha">Fecha</label>
    <input type="date" id="fecha" name="fecha" value="<c:out value='${filtroFecha}'/>">
    <button type="submit" class="btn btn-sec">Filtrar</button>
    <a href="${ctx}/citas" class="btn btn-sec">Limpiar</a>
</form>

<c:choose>
    <c:when test="${empty citas}"><p class="vacio">No hay citas para mostrar.</p></c:when>
    <c:otherwise>
        <div class="tabla-wrap">
            <table>
                <thead>
                <tr><th>Código</th><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Profesional</th><th>Motivo</th><th>Estado</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                <c:forEach var="c" items="${citas}">
                    <tr>
                        <td><c:out value="${c.codigo}"/></td>
                        <td><c:out value="${c.fecha}"/></td>
                        <td><c:out value="${c.hora}"/></td>
                        <td><c:out value="${c.paciente.nombre}"/></td>
                        <td><c:out value="${c.profesional.nombre}"/></td>
                        <td><c:out value="${c.motivo}"/></td>
                        <td><span class="estado estado-${c.estado}"><c:out value="${c.estado.etiqueta}"/></span></td>
                        <td class="celda-acciones">
                            <c:if test="${c.confirmable and (usuario.admin or usuario.profesional)}">
                                <form action="${ctx}/citas" method="post" class="en-linea">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="accion" value="confirmar">
                                    <input type="hidden" name="codigo" value="${c.codigo}">
                                    <button type="submit" class="btn btn-peq">Confirmar</button>
                                </form>
                            </c:if>
                            <c:if test="${c.atendible and usuario.profesional}">
                                <form action="${ctx}/citas" method="post" class="en-linea">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="accion" value="atender">
                                    <input type="hidden" name="codigo" value="${c.codigo}">
                                    <button type="submit" class="btn btn-peq">Atender</button>
                                </form>
                            </c:if>
                            <c:if test="${c.cancelable}">
                                <form action="${ctx}/citas" method="post" class="en-linea"
                                      onsubmit="return confirm('¿Cancelar la cita ${c.codigo}?');">
                                    <input type="hidden" name="csrf" value="${csrf}">
                                    <input type="hidden" name="accion" value="cancelar">
                                    <input type="hidden" name="codigo" value="${c.codigo}">
                                    <button type="submit" class="btn btn-peq btn-peligro">Cancelar</button>
                                </form>
                            </c:if>
                            <c:if test="${c.atendida}">
                                <a class="btn btn-peq btn-sec" href="${ctx}/seguimientos?cita=${c.codigo}">Seguimiento</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>
<%@ include file="fragments/pie.jspf" %>
