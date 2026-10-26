<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Forward directly to HomeServlet so standard MVC cycle triggers
    response.sendRedirect(request.getContextPath() + "/home");
%>
