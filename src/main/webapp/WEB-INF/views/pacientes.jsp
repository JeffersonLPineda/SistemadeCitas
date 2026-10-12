<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<div class="encabezado">
    <h1>Pacientes</h1>
    <c:if test="${usuario.admin and not mostrarForm}">
        <a class="btn" href="${ctx}/pacientes?accion=nuevo">Nuevo paciente</a>
    </c:if>
</div>

<c:if test="${mostrarForm}">
    <section class="tarjeta-info">
        <h2><c:choose><c:when test="${not empty paciente}">Editar paciente <c:out value="${paciente.codigo}"/></c:when><c:otherwise>Nuevo paciente</c:otherwise></c:choose></h2>
        <form action="${ctx}/pacientes" method="post" class="formulario">
            <input type="hidden" name="csrf" value="${csrf}">
            <c:choose>
                <c:when test="${not empty paciente}">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="codigo" value="<c:out value='${paciente.codigo}'/>">
                </c:when>
                <c:otherwise><input type="hidden" name="accion" value="crear"></c:otherwise>
            </c:choose>

            <label for="nombre">Nombre completo *</label>
            <input type="text" id="nombre" name="nombre" required maxlength="100" value="<c:out value='${form.nombre}'/>">

            <label for="telefono">Teléfono</label>
            <input type="tel" id="telefono" name="telefono" maxlength="20" placeholder="5555-5555" value="<c:out value='${form.telefono}'/>">

            <label for="correo">Correo electrónico *</label>
            <input type="email" id="correo" name="correo" required maxlength="100" value="<c:out value='${form.correo}'/>">

            <label for="direccion">Dirección</label>
            <input type="text" id="direccion" name="direccion" maxlength="200" value="<c:out value='${form.direccion}'/>">

            <c:if test="${empty paciente}">
                <label for="password">Contraseña inicial (opcional)</label>
                <input type="password" id="password" name="password" minlength="8" maxlength="100" autocomplete="new-password">
                <small>Si la escribe (mínimo 8 caracteres) se crea el acceso del paciente con su correo como usuario.</small>
            </c:if>

            <div class="acciones">
                <button type="submit" class="btn">Guardar</button>
                <a class="btn btn-sec" href="${ctx}/pacientes">Cancelar</a>
            </div>
        </form>
    </section>
</c:if>

<div class="tabla-wrap">
    <table>
        <thead><tr><th>Código</th><th>Nombre</th><th>Teléfono</th><th>Correo</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${pacientes}">
            <tr>
                <td><c:out value="${p.codigo}"/></td>
                <td><c:out value="${p.nombre}"/></td>
                <td><c:out value="${p.telefono}"/></td>
                <td><c:out value="${p.correo}"/></td>
                <td><span class="estado estado-${p.estado}"><c:out value="${p.estado.etiqueta}"/></span></td>
                <td class="celda-acciones">
                    <a class="btn btn-peq btn-sec" href="${ctx}/historial?paciente=${p.codigo}">Historial</a>
                    <c:if test="${usuario.admin}">
                        <a class="btn btn-peq btn-sec" href="${ctx}/pacientes?accion=editar&amp;codigo=${p.codigo}">Editar</a>
                        <form action="${ctx}/pacientes" method="post" class="en-linea">
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
