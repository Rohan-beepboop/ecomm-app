package com.shop.filter;

import com.shop.util.Flash;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cross-cutting concerns applied to every dynamic request:
 *  1. UTF-8 request decoding (must run before any parameter is read).
 *  2. CSRF protection: a per-session random token is exposed as ${csrfToken}; every POST must echo it
 *     back in the "_csrf" field, otherwise it is rejected with 403. This stops a malicious third-party
 *     page from silently submitting forms using the victim's logged-in session cookie.
 *  3. Flash messages: moves a one-shot message from the session into the request for the view.
 */
@WebFilter("/*")
public class CoreRequestFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse res = (HttpServletResponse) sres;
        req.setCharacterEncoding("UTF-8");

        String path = req.getServletPath();
        // Static assets must not consume the flash message or create sessions.
        if (path.startsWith("/css/") || path.startsWith("/js/")) {
            chain.doFilter(sreq, sres);
            return;
        }

        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            byte[] b = new byte[32];
            RANDOM.nextBytes(b);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            session.setAttribute("csrfToken", token);
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sent = req.getParameter("_csrf");
            boolean ok = sent != null && MessageDigest.isEqual(
                    token.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8));
            if (!ok) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token. Reload the page and try again.");
                return;
            }
        }

        Object flash = session.getAttribute(Flash.KEY);
        if (flash != null) {
            req.setAttribute(Flash.KEY, flash);
            session.removeAttribute(Flash.KEY);
        }
        chain.doFilter(sreq, sres);
    }
}
