<%
    // Punto de entrada: el filtro de autenticación decide si va al login o al panel.
    response.sendRedirect(request.getContextPath() + "/dashboard");
%>
