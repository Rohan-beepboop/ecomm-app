<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<div class="row justify-content-center">
  <div class="col-md-6 col-lg-4">
    <h1 class="h3 heading mb-3">Log in</h1>
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${ctx}/login" novalidate>
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <div class="mb-3">
        <label for="email" class="form-label">E-mail</label>
        <input type="email" class="form-control" id="email" name="email" value="<c:out value='${email}'/>" required autofocus>
      </div>
      <div class="mb-3">
        <label for="password" class="form-label">Password</label>
        <input type="password" class="form-control" id="password" name="password" required>
      </div>
      <button class="btn btn-accent w-100" type="submit">Log in</button>
    </form>
    <p class="mt-3 small">New here? <a href="${ctx}/register">Create an account</a></p>
  </div>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
