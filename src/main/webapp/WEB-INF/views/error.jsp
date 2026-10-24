<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<c:set var="pageTitle" value="Something went wrong" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<div class="text-center py-5">
  <h1 class="display-5 heading"><c:out value="${empty code ? 'Error' : code}"/></h1>
  <c:choose>
    <c:when test="${code == 404}"><p class="lead">We could not find that page.</p></c:when>
    <c:when test="${code == 403}"><p class="lead"><c:out value="${empty requestScope['jakarta.servlet.error.message'] ? 'You do not have access to this page.' : requestScope['jakarta.servlet.error.message']}"/></p></c:when>
    <c:otherwise><p class="lead">Something went wrong on our side. Please try again in a moment.</p></c:otherwise>
  </c:choose>
  <a class="btn btn-accent" href="${ctx}/catalog">Back to the shop</a>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
