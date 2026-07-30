package com.rushi.securebank.web;

import com.rushi.securebank.service.BankingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET  /register -> show the account opening form
 * POST /register -> create the customer, then send them to login
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            customerService.registerCustomer(username, password);
            redirect(request, response, "/login?registered");
        } catch (BankingException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, "register.jsp");
        }
    }
}
