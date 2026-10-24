<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/fragments/taglibs.jspf" %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<h1 class="h2 heading mb-4">My orders</h1>
<c:choose>
  <c:when test="${empty orders}">
    <p class="lead">You have not placed any orders yet.</p>
    <a class="btn btn-accent" href="${ctx}/catalog">Start shopping</a>
  </c:when>
  <c:otherwise>
    <c:forEach var="o" items="${orders}">
      <section class="card mb-3">
        <div class="card-header d-flex flex-wrap justify-content-between gap-2">
          <strong>Order #${o.id}</strong>
          <span class="text-muted"><fmt:formatDate value="${o.createdAt}" pattern="d MMM yyyy, HH:mm"/></span>
          <span class="badge text-bg-success"><c:out value="${o.status}"/></span>
        </div>
        <div class="card-body">
          <table class="table table-sm mb-2">
            <tbody>
            <c:forEach var="i" items="${o.items}">
              <tr>
                <td><c:out value="${i.productName}"/> &times; ${i.quantity}</td>
                <td class="text-end"><fmt:formatNumber value="${i.lineTotal}" type="currency" currencySymbol="$"/></td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
          <div class="d-flex justify-content-between">
            <span class="small text-muted">Ship to: <c:out value="${o.shippingAddress}"/></span>
            <strong><fmt:formatNumber value="${o.total}" type="currency" currencySymbol="$"/></strong>
          </div>
        </div>
      </section>
    </c:forEach>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
