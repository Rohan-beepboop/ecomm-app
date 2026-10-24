package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.model.Cart;
import com.shop.model.Product;
import com.shop.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Session-based cart controller. Guests may fill a cart; checkout (a different, protected URL) requires login.
 * POST actions: add | update | remove | clear. All follow Post/Redirect/Get so a browser refresh never repeats them.
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Cart.from(req.getSession(true)); // guarantees ${cart} exists for the view
        req.setAttribute("pageTitle", "Your cart");
        req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        Cart cart = Cart.from(session);
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");
        String back = "/cart";

        try {
            switch (action) {
                case "add": {
                    Product p = productDAO.findById(parseInt(req.getParameter("productId"), -1));
                    back = safeReturn(req.getParameter("returnUrl"), "/catalog");
                    if (p == null) Flash.set(session, "danger", "That product no longer exists.");
                    else if (p.getStock() < 1) Flash.set(session, "warning", "\"" + p.getName() + "\" is out of stock.");
                    else {
                        cart.add(p, parseInt(req.getParameter("quantity"), 1));
                        Flash.set(session, "success", "Added \"" + p.getName() + "\" to your cart.");
                    }
                    break;
                }
                case "update": {
                    Product p = productDAO.findById(parseInt(req.getParameter("productId"), -1));
                    if (p == null) { cart.remove(parseInt(req.getParameter("productId"), -1)); }
                    else {
                        int qty = parseInt(req.getParameter("quantity"), 1);
                        cart.setQuantity(p, qty);
                        if (qty > p.getStock()) Flash.set(session, "warning", "Only " + p.getStock() + " of \"" + p.getName() + "\" available; quantity adjusted.");
                    }
                    break;
                }
                case "remove":
                    cart.remove(parseInt(req.getParameter("productId"), -1));
                    Flash.set(session, "info", "Item removed from your cart.");
                    break;
                case "clear":
                    cart.clear();
                    Flash.set(session, "info", "Your cart is now empty.");
                    break;
                default:
                    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown cart action");
                    return;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error while updating the cart", e);
        }
        res.sendRedirect(req.getContextPath() + back);
    }

    /** Open-redirect guard: only app-relative paths ("/x", not "//host") are honoured. */
    private String safeReturn(String url, String fallback) {
        return (url != null && url.startsWith("/") && !url.startsWith("//") && !url.contains("\\")
                && !url.contains("\r") && !url.contains("\n")) ? url : fallback;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (RuntimeException e) { return def; }
    }
}
