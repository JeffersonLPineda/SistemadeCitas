<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<h1>Nueva cita</h1>
<section class="tarjeta-info">
    <form action="${ctx}/citas" method="post" class="formulario">
        <input type="hidden" name="csrf" value="${csrf}">
        <input type="hidden" name="accion" value="crear">

        <c:choose>
            <c:when test="${usuario.paciente}">
                <p>Paciente: <strong><c:out value="${pacienteActual.nombre}"/></strong> (<c:out value="${pacienteActual.codigo}"/>)</p>
            </c:when>
            <c:otherwise>
                <label for="codigoPaciente">Paciente *</label>
                <select id="codigoPaciente" name="codigoPaciente" required>
                    <option value="">— Seleccione —</option>
                    <c:forEach var="p" items="${pacientes}">
                        <option value="${p.codigo}" ${p.codigo == form.codigoPaciente ? 'selected' : ''}><c:out value="${p.codigo} · ${p.nombre}"/></option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>

        <label for="codigoProfesional">Profesional *</label>
        <select id="codigoProfesional" name="codigoProfesional" required>
            <option value="">— Seleccione —</option>
            <c:forEach var="p" items="${profesionales}">
                <option value="${p.codigo}" ${p.codigo == form.codigoProfesional ? 'selected' : ''}>
                    <c:out value="${p.nombre} · ${p.especialidad} (${p.disponibilidad.resumen})"/>
                </option>
            </c:forEach>
        </select>

        <div class="fila-2">
            <div>
                <label for="fecha">Fecha *</label>
                <input type="date" id="fecha" name="fecha" required min="${hoy}" value="<c:out value='${form.fecha}'/>">
            </div>
            <div>
                <label for="hora">Hora *</label>
                <input type="time" id="hora" name="hora" required step="1800" value="<c:out value='${form.hora}'/>">
            </div>
        </div>
        <small>Las citas se programan en bloques de 30 minutos, dentro de la disponibilidad del profesional.</small>

        <label for="motivo">Motivo de la consulta *</label>
        <textarea id="motivo" name="motivo" rows="3" required maxlength="300"><c:out value="${form.motivo}"/></textarea>

        <div class="acciones">
            <button type="submit" class="btn">Programar cita</button>
            <a class="btn btn-sec" href="${ctx}/citas">Cancelar</a>
        </div>
    </form>
</section>
<%@ include file="fragments/pie.jspf" %>
