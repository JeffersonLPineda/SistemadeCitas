<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<div class="encabezado">
    <h1>Profesionales</h1>
    <c:if test="${usuario.admin and not mostrarForm}">
        <a class="btn" href="${ctx}/profesionales?accion=nuevo">Nuevo profesional</a>
    </c:if>
</div>

<c:if test="${mostrarForm}">
    <section class="tarjeta-info">
        <h2><c:choose><c:when test="${not empty profesional}">Editar profesional <c:out value="${profesional.codigo}"/></c:when><c:otherwise>Nuevo profesional</c:otherwise></c:choose></h2>
        <form action="${ctx}/profesionales" method="post" class="formulario">
            <input type="hidden" name="csrf" value="${csrf}">
            <c:choose>
                <c:when test="${not empty profesional}">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="codigo" value="<c:out value='${profesional.codigo}'/>">
                </c:when>
                <c:otherwise><input type="hidden" name="accion" value="crear"></c:otherwise>
            </c:choose>

            <label for="nombre">Nombre *</label>
            <input type="text" id="nombre" name="nombre" required maxlength="100" value="<c:out value='${form.nombre}'/>">

            <label for="especialidad">Especialidad *</label>
            <input type="text" id="especialidad" name="especialidad" required maxlength="100" value="<c:out value='${form.especialidad}'/>">

            <fieldset>
                <legend>Días de atención *</legend>
                <div class="dias">
                    <label><input type="checkbox" name="dias" value="MONDAY" ${fn:contains(form.dias, 'MONDAY') ? 'checked' : ''}> Lun</label>
                    <label><input type="checkbox" name="dias" value="TUESDAY" ${fn:contains(form.dias, 'TUESDAY') ? 'checked' : ''}> Mar</label>
                    <label><input type="checkbox" name="dias" value="WEDNESDAY" ${fn:contains(form.dias, 'WEDNESDAY') ? 'checked' : ''}> Mié</label>
                    <label><input type="checkbox" name="dias" value="THURSDAY" ${fn:contains(form.dias, 'THURSDAY') ? 'checked' : ''}> Jue</label>
                    <label><input type="checkbox" name="dias" value="FRIDAY" ${fn:contains(form.dias, 'FRIDAY') ? 'checked' : ''}> Vie</label>
                    <label><input type="checkbox" name="dias" value="SATURDAY" ${fn:contains(form.dias, 'SATURDAY') ? 'checked' : ''}> Sáb</label>
                    <label><input type="checkbox" name="dias" value="SUNDAY" ${fn:contains(form.dias, 'SUNDAY') ? 'checked' : ''}> Dom</label>
                </div>
            </fieldset>

            <div class="fila-2">
                <div>
                    <label for="horaInicio">Hora de inicio *</label>
                    <input type="time" id="horaInicio" name="horaInicio" required step="1800" value="<c:out value='${form.horaInicio}'/>">
                </div>
                <div>
                    <label for="horaFin">Hora de fin *</label>
                    <input type="time" id="horaFin" name="horaFin" required step="1800" value="<c:out value='${form.horaFin}'/>">
                </div>
            </div>

            <c:if test="${empty profesional}">
                <label for="correoAcceso">Correo de acceso (opcional)</label>
                <input type="email" id="correoAcceso" name="correoAcceso" maxlength="100" value="<c:out value='${form.correoAcceso}'/>">
                <label for="password">Contraseña inicial (opcional)</label>
                <input type="password" id="password" name="password" minlength="8" maxlength="100" autocomplete="new-password">
                <small>Para crear el acceso del profesional indique correo y contraseña (mínimo 8 caracteres).</small>
            </c:if>

            <div class="acciones">
                <button type="submit" class="btn">Guardar</button>
                <a class="btn btn-sec" href="${ctx}/profesionales">Cancelar</a>
            </div>
        </form>
    </section>
</c:if>

<div class="tabla-wrap">
    <table>
        <thead><tr><th>Código</th><th>Nombre</th><th>Especialidad</th><th>Disponibilidad</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${profesionales}">
            <tr>
                <td><c:out value="${p.codigo}"/></td>
                <td><c:out value="${p.nombre}"/></td>
                <td><c:out value="${p.especialidad}"/></td>
                <td><c:out value="${p.disponibilidad.resumen}"/></td>
                <td><span class="estado estado-${p.estado}"><c:out value="${p.estado.etiqueta}"/></span></td>
                <td class="celda-acciones">
                    <c:if test="${(usuario.admin or usuario.paciente) and p.activo}">
                        <a class="btn btn-peq" href="${ctx}/citas?accion=nueva&amp;profesional=${p.codigo}">Solicitar cita</a>
                    </c:if>
                    <c:if test="${usuario.admin}">
                        <a class="btn btn-peq btn-sec" href="${ctx}/profesionales?accion=editar&amp;codigo=${p.codigo}">Editar</a>
                        <form action="${ctx}/profesionales" method="post" class="en-linea">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <input type="hidden" name="accion" value="estado">
                            <input type="hidden" name="codigo" value="<c:out value='${p.codigo}'/>">
                            <input type="hidden" name="estado" value="${p.activo ? 'INACTIVO' : 'ACTIVO'}">
                            <button type="submit" class="btn btn-peq btn-sec">${p.activo ? 'Desactivar' : 'Activar'}</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<%@ include file="fragments/pie.jspf" %>
