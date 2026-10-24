package com.shop.filter;

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

/** Route protection: pages that require ANY logged-in user (checkout, order history). */
@WebFilter(urlPatterns = { "/checkout", "/orders" })
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse res = (HttpServletResponse) sres;
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            // Remember where the visitor was heading so login can send them back (GET only; a POST cannot be replayed).
            HttpSession s = req.getSession(true);
            if ("GET".equalsIgnoreCase(req.getMethod())) s.setAttribute("returnTo", req.getServletPath());
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(sreq, sres);
    }
}
