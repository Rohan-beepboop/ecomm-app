<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<div class="row justify-content-center">
  <div class="col-md-7 col-lg-5">
    <h1 class="h3 heading mb-3">Create your account</h1>
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${ctx}/register" novalidate>
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <div class="mb-3">
        <label for="fullName" class="form-label">Full name</label>
        <input class="form-control" id="fullName" name="fullName" maxlength="100" value="<c:out value='${fullName}'/>" required autofocus>
      </div>
      <div class="mb-3">
        <label for="email" class="form-label">E-mail</label>
        <input type="email" class="form-control" id="email" name="email" maxlength="150" value="<c:out value='${email}'/>" required>
      </div>
      <div class="mb-3">
        <label for="password" class="form-label">Password</label>
        <input type="password" class="form-control" id="password" name="password" minlength="8" required aria-describedby="pwHelp">
        <div id="pwHelp" class="form-text">At least 8 characters.</div>
      </div>
      <div class="mb-3">
        <label for="confirmPassword" class="form-label">Confirm password</label>
        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
      </div>
      <button class="btn btn-accent w-100" type="submit">Create account</button>
    </form>
    <p class="mt-3 small">Already registered? <a href="${ctx}/login">Log in</a></p>
  </div>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
