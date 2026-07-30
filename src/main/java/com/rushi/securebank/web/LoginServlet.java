package com.rushi.securebank.web;

import com.rushi.securebank.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * GET  /login  -> show the login page
 * POST /login  -> verify credentials and start a session
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (currentCustomer(request) != null) {
            redirect(request, response, "/dashboard");
            return;
        }
        forward(request, response, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Customer customer = customerService.authenticate(username, password);

        if (customer == null) {
            redirect(request, response, "/login?error");
            return;
        }

        // Guard against session fixation: drop any old session first.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, customer.getUsername());
        session.setMaxInactiveInterval(30 * 60);

        redirect(request, response, "/dashboard");
    }
}
