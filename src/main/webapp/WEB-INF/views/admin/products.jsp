<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<div class="d-flex justify-content-between align-items-center mb-3">
  <h1 class="h2 heading mb-0">Inventory</h1>
  <a class="btn btn-accent" href="${ctx}/admin/products?action=new">Add product</a>
</div>
<div class="table-responsive">
  <table class="table table-hover align-middle">
    <thead><tr><th>Product</th><th>Category</th><th class="text-end">Price</th><th class="text-end">Stock</th><th class="text-end">Actions</th></tr></thead>
    <tbody>
    <c:forEach var="p" items="${products}">
      <tr>
        <td><div class="d-flex align-items-center gap-3">
            <img class="thumb" src="<c:out value='${p.imageUrl}'/>" alt="">
            <span><c:out value="${p.name}"/></span></div></td>
        <td><c:out value="${p.categoryName}"/></td>
        <td class="text-end"><fmt:formatNumber value="${p.price}" type="currency" currencySymbol="$"/></td>
        <td class="text-end ${p.stock <= 5 ? 'text-danger fw-semibold' : ''}">${p.stock}</td>
        <td class="text-end">
          <div class="d-inline-flex gap-2">
            <a class="btn btn-sm btn-outline-ink" href="${ctx}/admin/products?action=edit&amp;id=${p.id}">Edit</a>
            <form method="post" action="${ctx}/admin/products" data-confirm="Delete &quot;${fn:escapeXml(p.name)}&quot;? This cannot be undone.">
              <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
              <input type="hidden" name="action" value="delete">
              <input type="hidden" name="id" value="${p.id}">
              <button class="btn btn-sm btn-outline-danger" type="submit">Delete</button>
            </form>
          </div>
        </td>
      </tr>
    </c:forEach>
    <c:if test="${empty products}"><tr><td colspan="5" class="text-center py-4">No products yet. Add the first one.</td></tr></c:if>
    </tbody>
  </table>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
