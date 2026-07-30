package com.rushi.securebank.filter;

import com.rushi.securebank.web.BaseServlet;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ================================================================
 *  AuthFilter - replaces Spring Security's filter chain
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Everything requires a session except the paths listed in
 *  PUBLIC_PATHS. Unauthenticated visitors are bounced to /login.
 *
 *  This filter runs on REQUEST dispatches only (the default), so
 *  the servlets can still forward to JSPs under /WEB-INF/views
 *  without being intercepted.
 * ================================================================
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "", "/", "/index.jsp", "/login", "/register", "/health", "/favicon.ico"));

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (PUBLIC_PATHS.contains(path) || path.startsWith("/static/") || path.startsWith("/assets/")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(BaseServlet.SESSION_USER) != null;

        if (loggedIn) {
            chain.doFilter(req, res);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
