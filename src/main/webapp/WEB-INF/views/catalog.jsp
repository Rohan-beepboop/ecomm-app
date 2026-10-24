<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<div class="d-flex flex-column flex-md-row justify-content-between align-items-md-end gap-3 mb-4">
  <h1 class="h2 heading mb-0">Everyday tools, well made</h1>
  <form class="d-flex gap-2" method="get" action="${ctx}/catalog" role="search">
    <c:if test="${not empty categoryId}"><input type="hidden" name="category" value="<c:out value='${categoryId}'/>"></c:if>
    <input class="form-control" type="search" name="q" placeholder="Search products" aria-label="Search products" value="<c:out value='${q}'/>">
    <button class="btn btn-accent" type="submit">Search</button>
  </form>
</div>

<div class="d-flex flex-wrap gap-2 mb-4" aria-label="Categories">
  <a class="btn btn-sm ${empty categoryId ? 'btn-ink' : 'btn-outline-ink'}" href="${ctx}/catalog<c:if test='${not empty q}'>?q=<c:out value='${q}'/></c:if>">All</a>
  <c:forEach var="cat" items="${categories}">
    <a class="btn btn-sm ${categoryId == cat.id ? 'btn-ink' : 'btn-outline-ink'}"
       href="${ctx}/catalog?category=${cat.id}<c:if test='${not empty q}'>&amp;q=<c:out value='${q}'/></c:if>"><c:out value="${cat.name}"/></a>
  </c:forEach>
</div>

<c:choose>
  <c:when test="${empty products}">
    <div class="text-center py-5">
      <p class="lead mb-2">No products match your search.</p>
      <a href="${ctx}/catalog">Clear search and filters</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 row-cols-xl-4 g-4">
      <c:forEach var="p" items="${products}">
        <div class="col">
          <article class="card product h-100">
            <img src="<c:out value='${p.imageUrl}'/>" class="card-img-top" alt="<c:out value='${p.name}'/>" loading="lazy">
            <div class="card-body d-flex flex-column">
              <span class="small text-muted"><c:out value="${p.categoryName}"/></span>
              <h2 class="h5 card-title heading"><c:out value="${p.name}"/></h2>
              <p class="card-text small flex-grow-1"><c:out value="${p.description}"/></p>
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="price"><fmt:formatNumber value="${p.price}" type="currency" currencySymbol="$"/></span>
                <c:choose>
                  <c:when test="${p.stock == 0}"><span class="badge text-bg-secondary">Out of stock</span></c:when>
                  <c:when test="${p.stock <= 5}"><span class="badge text-bg-warning">Only ${p.stock} left</span></c:when>
                </c:choose>
              </div>
              <form method="post" action="${ctx}/cart">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="productId" value="${p.id}">
                <input type="hidden" name="quantity" value="1">
                <input type="hidden" name="returnUrl" value="<c:out value='${currentUrl}'/>">
                <button class="btn btn-accent w-100" type="submit" ${p.stock == 0 ? 'disabled' : ''}>Add to cart</button>
              </form>
            </div>
          </article>
        </div>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
