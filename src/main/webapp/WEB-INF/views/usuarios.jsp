<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/proteger.jspf" %>
<%@ include file="fragments/cabecera.jspf" %>
<h1>Usuarios</h1>
<p class="nota">Los accesos de pacientes y profesionales se crean al registrarlos. Aquí puede activarlos, desactivarlos o restablecer su contraseña.</p>
<div class="tabla-wrap">
    <table>
        <thead><tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Rol</th><th>Registro asociado</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
        <c:forEach var="u" items="${usuarios}">
            <tr>
                <td><c:out value="${u.id}"/></td>
                <td><c:out value="${u.nombre}"/></td>
                <td><c:out value="${u.correo}"/></td>
                <td><c:out value="${u.rol.etiqueta}"/></td>
                <td><c:out value="${empty u.codigoAsociado ? '—' : u.codigoAsociado}"/></td>
                <td><span class="estado estado-${u.estado}"><c:out value="${u.estado.etiqueta}"/></span></td>
                <td class="celda-acciones">
                    <form action="${ctx}/usuarios" method="post" class="en-linea">
                        <input type="hidden" name="csrf" value="${csrf}">
                        <input type="hidden" name="accion" value="estado">
                        <input type="hidden" name="id" value="${u.id}">
                        <input type="hidden" name="estado" value="${u.activo ? 'INACTIVO' : 'ACTIVO'}">
                        <button type="submit" class="btn btn-peq btn-sec">${u.activo ? 'Desactivar' : 'Activar'}</button>
                    </form>
                    <form action="${ctx}/usuarios" method="post" class="en-linea">
                        <input type="hidden" name="csrf" value="${csrf}">
                        <input type="hidden" name="accion" value="password">
                        <input type="hidden" name="id" value="${u.id}">
                        <input type="password" name="nuevaPassword" placeholder="Nueva contraseña" minlength="8" maxlength="100"
                               required autocomplete="new-password" aria-label="Nueva contraseña de ${u.nombre}">
                        <button type="submit" class="btn btn-peq">Restablecer</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<%@ include file="fragments/pie.jspf" %>
