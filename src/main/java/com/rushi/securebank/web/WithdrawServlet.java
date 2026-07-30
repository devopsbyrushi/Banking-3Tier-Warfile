package com.rushi.securebank.web;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.service.BankingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /withdraw -> debit funds from the logged-in account.
 * Shows the "Insufficient funds" alert on the dashboard when it fails.
 */
@WebServlet(name = "WithdrawServlet", urlPatterns = "/withdraw")
public class WithdrawServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Customer customer = currentCustomer(request);
        if (customer == null) {
            redirect(request, response, "/login");
            return;
        }

        try {
            customerService.withdraw(customer, amountParam(request, "amount"));
            redirect(request, response, "/dashboard");
        } catch (BankingException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("customer", currentCustomer(request));
            forward(request, response, "dashboard.jsp");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        redirect(request, response, "/dashboard");
    }
}
