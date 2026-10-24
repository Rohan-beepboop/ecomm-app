package com.shop.controller;

import com.shop.dao.UserDAO;
import com.shop.model.User;
import com.shop.util.Flash;
import com.shop.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

/** Controller for registration, login and logout. Contains no HTML; it only routes to JSP views. */
@WebServlet(urlPatterns = { "/login", "/register", "/logout" })
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // Verified against when the e-mail is unknown, so "no such user" and "wrong password" cost the same time.
    private static final String DUMMY_HASH = PasswordUtil.hash("dummy-password-for-timing");

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/logout":
                HttpSession s = req.getSession(false);
                if (s != null) s.invalidate(); // destroys user + cart server-side
                // Fresh session so we can show a confirmation message after the redirect.
                Flash.set(req.getSession(true), "info", "You have been logged out.");
                res.sendRedirect(req.getContextPath() + "/catalog");
                return;
            case "/register":
                req.setAttribute("pageTitle", "Create account");
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
                return;
            default:
                req.setAttribute("pageTitle", "Log in");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            switch (req.getServletPath()) {
                case "/register": handleRegister(req, res); break;
                case "/login":    handleLogin(req, res);    break;
                default: res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during authentication", e); // -> 500 error page
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        String name = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        String error = null;
        if (name.length() < 2 || name.length() > 100) error = "Enter your full name (2-100 characters).";
        else if (!EMAIL.matcher(email).matches() || email.length() > 150) error = "Enter a valid e-mail address.";
        else if (password.length() < 8) error = "Password must be at least 8 characters.";
        else if (!password.equals(confirm)) error = "The two passwords do not match.";

        if (error == null) {
            User u = new User();
            u.setFullName(name);
            u.setEmail(email);
            u.setPasswordHash(PasswordUtil.hash(password));
            u.setRole(User.ROLE_CUSTOMER); // role is NEVER taken from the request: no privilege escalation by form tampering
            try {
                userDAO.insert(u);
                Flash.set(req.getSession(true), "success", "Account created. Please log in.");
                res.sendRedirect(req.getContextPath() + "/login");
                return;
            } catch (SQLIntegrityConstraintViolationException dup) {
                error = "An account with that e-mail already exists.";
            }
        }
        // Re-render the form, keeping what was typed (except passwords).
        req.setAttribute("error", error);
        req.setAttribute("fullName", name);
        req.setAttribute("email", email);
        req.setAttribute("pageTitle", "Create account");
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        String email = trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        User user = userDAO.findByEmail(email);
        boolean ok = PasswordUtil.verify(password, user == null ? DUMMY_HASH : user.getPasswordHash()) && user != null;

        if (!ok) {
            // One generic message: never reveal whether the e-mail exists (prevents account enumeration).
            req.setAttribute("error", "Incorrect e-mail or password.");
            req.setAttribute("email", email);
            req.setAttribute("pageTitle", "Log in");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            return;
        }

        HttpSession session = req.getSession(true);
        // Session-fixation defence: issue a new session id at the privilege boundary. Attributes
        // (including a guest's cart) are preserved by changeSessionId().
        req.changeSessionId();
        session.setAttribute("user", user.toSessionUser());

        String returnTo = (String) session.getAttribute("returnTo");
        session.removeAttribute("returnTo");
        String target = user.isAdmin() ? "/admin/products" : (returnTo != null ? returnTo : "/catalog");
        res.sendRedirect(req.getContextPath() + target);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
