<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" %><%
    // Entry point: always start at the login page.
    response.sendRedirect(request.getContextPath() + "/login");
%>
