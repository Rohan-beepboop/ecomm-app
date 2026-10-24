package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.model.Product;
import com.shop.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin inventory CRUD. Access is enforced by AdminFilter (/admin/*), so this class can assume an admin caller.
 *   GET  ?action=list (default) | new | edit&id=N
 *   POST action=save (insert when id is blank, update otherwise) | delete
 */
@WebServlet("/admin/products")
public class AdminProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
        try {
            switch (action) {
                case "new":
                    showForm(req, res, new Product(), "Add product");
                    break;
                case "edit": {
                    Product p = productDAO.findById(parseInt(req.getParameter("id"), -1));
                    if (p == null) { res.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                    showForm(req, res, p, "Edit product");
                    break;
                }
                default:
                    req.setAttribute("products", productDAO.search(null, null));
                    req.setAttribute("pageTitle", "Inventory");
                    req.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in admin dashboard", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");
        try {
            if ("delete".equals(action)) {
                boolean gone = productDAO.delete(parseInt(req.getParameter("id"), -1));
                Flash.set(req.getSession(), gone ? "success" : "warning", gone ? "Product deleted." : "Product not found.");
                res.sendRedirect(req.getContextPath() + "/admin/products");
            } else if ("save".equals(action)) {
                save(req, res);
            } else {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in admin dashboard", e);
        }
    }

    private void save(HttpServletRequest req, HttpServletResponse res) throws SQLException, ServletException, IOException {
        Product p = new Product();
        p.setId(parseInt(req.getParameter("id"), 0));
        p.setName(val(req, "name"));
        p.setDescription(val(req, "description"));
        p.setImageUrl(val(req, "imageUrl"));
        p.setCategoryId(parseInt(req.getParameter("categoryId"), 0));
        p.setStock(parseInt(req.getParameter("stock"), -1));
        try { p.setPrice(new BigDecimal(val(req, "price")).setScale(2, java.math.RoundingMode.HALF_UP)); }
        catch (NumberFormatException e) { p.setPrice(null); }

        // Server-side validation is the real gate; HTML5 attributes on the form are only a convenience.
        List<String> errors = new ArrayList<>();
        if (p.getName().isEmpty() || p.getName().length() > 150) errors.add("Name is required (max 150 characters).");
        if (p.getPrice() == null || p.getPrice().signum() < 0 || p.getPrice().compareTo(new BigDecimal("99999999")) > 0)
            errors.add("Price must be a non-negative number.");
        if (p.getStock() < 0) errors.add("Stock must be a whole number, 0 or more.");
        if (p.getCategoryId() <= 0) errors.add("Choose a category.");
        if (!p.getImageUrl().isEmpty() && !p.getImageUrl().matches("(?i)^https?://\\S{1,480}$"))
            errors.add("Image URL must start with http:// or https://.");

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showForm(req, res, p, p.getId() > 0 ? "Edit product" : "Add product");
            return;
        }
        try {
            if (p.getId() > 0) productDAO.update(p); else productDAO.insert(p);
        } catch (java.sql.SQLIntegrityConstraintViolationException fk) {
            errors.add("The selected category does not exist.");
            req.setAttribute("errors", errors);
            showForm(req, res, p, "Add product");
            return;
        }
        Flash.set(req.getSession(), "success", "\"" + p.getName() + "\" saved.");
        res.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res, Product p, String title)
            throws SQLException, ServletException, IOException {
        req.setAttribute("product", p);
        req.setAttribute("categories", productDAO.findAllCategories());
        req.setAttribute("pageTitle", title);
        req.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(req, res);
    }

    private String val(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (RuntimeException e) { return def; }
    }
}
