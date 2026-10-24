package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.dao.OrderException;
import com.shop.model.Cart;
import com.shop.model.User;
import com.shop.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/** Simulated checkout. Protected by AuthFilter. This is the ONLY place the cart is committed to the database. */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (Cart.from(req.getSession()).isEmpty()) {
            Flash.set(req.getSession(), "info", "Your cart is empty.");
            res.sendRedirect(req.getContextPath() + "/catalog");
            return;
        }
        req.setAttribute("pageTitle", "Checkout");
        req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Cart cart = Cart.from(session);
        User user = (User) session.getAttribute("user");
        String address = req.getParameter("shippingAddress") == null ? "" : req.getParameter("shippingAddress").trim();

        if (address.length() < 10 || address.length() > 255) {
            req.setAttribute("error", "Enter a shipping address of 10-255 characters.");
            req.setAttribute("address", address);
            req.setAttribute("pageTitle", "Checkout");
            req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, res);
            return;
        }

        // Serialise checkouts per session so a double-click cannot place the same cart twice.
        synchronized (session) {
            try {
                int orderId = orderDAO.placeOrder(user.getId(), cart, address);
                cart.clear(); // order is safely committed; only now is the session cart discarded
                Flash.set(session, "success", "Thank you! Order #" + orderId + " has been placed.");
                res.sendRedirect(req.getContextPath() + "/orders");
            } catch (OrderException e) {
                Flash.set(session, "danger", e.getMessage()); // business rule failure: safe to show
                res.sendRedirect(req.getContextPath() + "/cart");
            } catch (SQLException e) {
                throw new ServletException("Could not place the order", e);
            }
        }
    }
}
