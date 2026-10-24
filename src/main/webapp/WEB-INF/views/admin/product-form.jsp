<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<div class="row justify-content-center">
  <div class="col-lg-8">
    <h1 class="h2 heading mb-3"><c:out value="${pageTitle}"/></h1>
    <c:if test="${not empty errors}">
      <div class="alert alert-danger"><ul class="mb-0">
        <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
      </ul></div>
    </c:if>
    <form method="post" action="${ctx}/admin/products" novalidate>
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <input type="hidden" name="action" value="save">
      <input type="hidden" name="id" value="${product.id > 0 ? product.id : ''}">
      <div class="mb-3">
        <label class="form-label" for="name">Name</label>
        <input class="form-control" id="name" name="name" maxlength="150" required value="<c:out value='${product.name}'/>">
      </div>
      <div class="row g-3 mb-3">
        <div class="col-md-4">
          <label class="form-label" for="categoryId">Category</label>
          <select class="form-select" id="categoryId" name="categoryId" required>
            <option value="">Choose...</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat.id}" ${product.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label" for="price">Price ($)</label>
          <input class="form-control" id="price" name="price" type="number" min="0" step="0.01" required value="<c:out value='${product.price}'/>">
        </div>
        <div class="col-md-4">
          <label class="form-label" for="stock">Stock</label>
          <input class="form-control" id="stock" name="stock" type="number" min="0" step="1" required value="${product.stock}">
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label" for="imageUrl">Image URL</label>
        <input class="form-control" id="imageUrl" name="imageUrl" type="url" maxlength="500" value="<c:out value='${product.imageUrl}'/>">
      </div>
      <div class="mb-3">
        <label class="form-label" for="description">Description</label>
        <textarea class="form-control" id="description" name="description" rows="4"><c:out value="${product.description}"/></textarea>
      </div>
      <button class="btn btn-accent" type="submit">Save product</button>
      <a class="btn btn-link" href="${ctx}/admin/products">Cancel</a>
    </form>
  </div>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
