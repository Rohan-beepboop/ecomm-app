package com.shop.controller;

import com.shop.dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Public product listing with keyword search (SQL LIKE) and category filtering. */
@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String q = req.getParameter("q");
        Integer categoryId = parseId(req.getParameter("category"));
        try {
            req.setAttribute("products", productDAO.search(q, categoryId));
            req.setAttribute("categories", productDAO.findAllCategories());
        } catch (SQLException e) {
            throw new ServletException("Could not load the catalog", e);
        }
        req.setAttribute("q", q == null ? "" : q.trim());
        req.setAttribute("categoryId", categoryId);
        // Lets the "Add to cart" forms send the shopper back to this exact filtered view.
        String qs = req.getQueryString();
        req.setAttribute("currentUrl", "/catalog" + (qs == null ? "" : "?" + qs));
        req.setAttribute("pageTitle", "Shop");
        req.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(req, res);
    }

    /** Tolerant parse: a garbage ?category= value simply means "no filter" rather than an error page. */
    private Integer parseId(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
