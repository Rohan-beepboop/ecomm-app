package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Shows the logged-in customer's own orders. The user id comes from the session, never from a parameter (no IDOR). */
@WebServlet("/orders")
public class OrderHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        try {
            req.setAttribute("orders", orderDAO.findByUser(user.getId()));
        } catch (SQLException e) {
            throw new ServletException("Could not load order history", e);
        }
        req.setAttribute("pageTitle", "My orders");
        req.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(req, res);
    }
}
