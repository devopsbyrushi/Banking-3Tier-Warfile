package com.rushi.securebank.web;

import com.rushi.securebank.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /dashboard -> account summary plus deposit / withdraw / transfer forms.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Customer customer = currentCustomer(request);
        if (customer == null) {
            redirect(request, response, "/login");
            return;
        }
        request.setAttribute("customer", customer);
        forward(request, response, "dashboard.jsp");
    }
}
