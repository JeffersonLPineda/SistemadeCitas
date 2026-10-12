<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="fragments/cabecera.jspf" %>
<section class="login">
    <h1>Iniciar sesión</h1>
    <c:if test="${param.salio == '1'}">
        <div class="alerta ok" role="status">Sesión cerrada correctamente.</div>
    </c:if>
    <form action="${ctx}/login" method="post" class="formulario" autocomplete="on">
        <label for="correo">Correo electrónico</label>
        <input type="email" id="correo" name="correo" required maxlength="100" autofocus
               value="<c:out value='${previo.correo}'/>">

        <label for="password">Contraseña</label>
        <input type="password" id="password" name="password" required maxlength="100" autocomplete="current-password">

        <button type="submit" class="btn">Ingresar</button>
    </form>
    <p class="nota">¿No tiene acceso? Solicítelo al administrador del sistema.</p>
</section>
<%@ include file="fragments/pie.jspf" %>
