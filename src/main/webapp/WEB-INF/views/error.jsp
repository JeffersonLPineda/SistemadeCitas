<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Error"/>
<%@ include file="fragments/cabecera.jspf" %>
<section class="tarjeta-info">
    <h1>No se pudo completar la acción</h1>
    <c:choose>
        <c:when test="${pageContext.errorData.statusCode == 403}">
            <p>No tiene permiso para ver esta página o realizar esta acción.</p>
        </c:when>
        <c:when test="${pageContext.errorData.statusCode == 404}">
            <p>La página solicitada no existe.</p>
        </c:when>
        <c:when test="${pageContext.errorData.statusCode == 405}">
            <p>Operación no permitida para esta dirección.</p>
        </c:when>
        <c:otherwise>
            <p>Ocurrió un error inesperado. Intente de nuevo; si persiste, avise al administrador.</p>
        </c:otherwise>
    </c:choose>
    <p><a class="btn" href="${ctx}/dashboard">Volver al panel</a></p>
</section>
<%@ include file="fragments/pie.jspf" %>
