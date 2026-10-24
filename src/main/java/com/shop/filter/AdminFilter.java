package com.shop.filter;

import com.shop.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Role-based access control for everything under /admin/. The role is read from the server-side session,
 * never from a request parameter or cookie the client could forge.
 * Unauthenticated -> login page; authenticated non-admin -> 403.
 */
@WebFilter("/admin/*")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse res = (HttpServletResponse) sres;
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            HttpSession s = req.getSession(true);
            if ("GET".equalsIgnoreCase(req.getMethod())) s.setAttribute("returnTo", req.getServletPath());
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!user.isAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access is required.");
            return;
        }
        // Never let a browser cache admin pages (e.g. pressing Back after logout).
        res.setHeader("Cache-Control", "no-store");
        chain.doFilter(sreq, sres);
    }
}
