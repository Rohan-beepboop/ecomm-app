<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<h1 class="h2 heading mb-4">Your cart</h1>
<c:choose>
  <c:when test="${empty cart}">
    <div class="text-center py-5">
      <p class="lead">Your cart is empty.</p>
      <a class="btn btn-accent" href="${ctx}/catalog">Browse products</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
      <table class="table align-middle">
        <thead><tr><th>Product</th><th class="text-end">Price</th><th class="text-center">Quantity</th><th class="text-end">Subtotal</th><th><span class="visually-hidden">Remove</span></th></tr></thead>
        <tbody>
        <c:forEach var="item" items="${cart.items}">
          <tr>
            <td>
              <div class="d-flex align-items-center gap-3">
                <img class="thumb" src="<c:out value='${item.imageUrl}'/>" alt="">
                <span><c:out value="${item.name}"/></span>
              </div>
            </td>
            <td class="text-end"><fmt:formatNumber value="${item.unitPrice}" type="currency" currencySymbol="$"/></td>
            <td class="text-center">
              <form method="post" action="${ctx}/cart" class="qty-form d-inline-flex align-items-center gap-1">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="productId" value="${item.productId}">
                <button type="button" class="btn btn-sm btn-outline-ink" data-step="-1" aria-label="Decrease quantity">&minus;</button>
                <input type="number" class="form-control form-control-sm text-center qty-input" name="quantity" value="${item.quantity}" min="1" max="${item.maxStock}" aria-label="Quantity">
                <button type="button" class="btn btn-sm btn-outline-ink" data-step="1" aria-label="Increase quantity">+</button>
              </form>
            </td>
            <td class="text-end"><fmt:formatNumber value="${item.lineTotal}" type="currency" currencySymbol="$"/></td>
            <td class="text-end">
              <form method="post" action="${ctx}/cart">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="productId" value="${item.productId}">
                <button class="btn btn-sm btn-link text-danger" type="submit">Remove</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        </tbody>
        <tfoot>
          <tr><th colspan="3" class="text-end">Total</th>
              <th class="text-end price"><fmt:formatNumber value="${cart.total}" type="currency" currencySymbol="$"/></th><th></th></tr>
        </tfoot>
      </table>
    </div>
    <div class="d-flex flex-wrap justify-content-between gap-2">
      <div class="d-flex gap-2">
        <a class="btn btn-outline-ink" href="${ctx}/catalog">Continue shopping</a>
        <form method="post" action="${ctx}/cart" data-confirm="Remove everything from your cart?">
          <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
          <input type="hidden" name="action" value="clear">
          <button class="btn btn-outline-danger" type="submit">Empty cart</button>
        </form>
      </div>
      <a class="btn btn-accent" href="${ctx}/checkout">Proceed to checkout</a>
    </div>
    <p class="small text-muted mt-3">Final prices and availability are confirmed when you place the order.</p>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
