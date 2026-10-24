<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<h1 class="h2 heading mb-4">Checkout</h1>
<div class="row g-4">
  <div class="col-lg-7">
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${ctx}/checkout" novalidate>
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <div class="mb-3">
        <label for="shippingAddress" class="form-label">Shipping address</label>
        <textarea class="form-control" id="shippingAddress" name="shippingAddress" rows="3" maxlength="255" required><c:out value="${address}"/></textarea>
      </div>
      <p class="small text-muted">This is a simulated checkout. No payment details are collected.</p>
      <button class="btn btn-accent" type="submit">Place order</button>
      <a class="btn btn-link" href="${ctx}/cart">Back to cart</a>
    </form>
  </div>
  <div class="col-lg-5">
    <div class="card"><div class="card-body">
      <h2 class="h5 heading">Order summary</h2>
      <ul class="list-unstyled mb-3">
        <c:forEach var="item" items="${cart.items}">
          <li class="d-flex justify-content-between py-1">
            <span><c:out value="${item.name}"/> &times; ${item.quantity}</span>
            <span><fmt:formatNumber value="${item.lineTotal}" type="currency" currencySymbol="$"/></span>
          </li>
        </c:forEach>
      </ul>
      <div class="d-flex justify-content-between border-top pt-2 fw-semibold">
        <span>Total</span><span class="price"><fmt:formatNumber value="${cart.total}" type="currency" currencySymbol="$"/></span>
      </div>
    </div></div>
  </div>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
