package com.rushi.securebank.web;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.model.TransactionHistory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * GET /transactions -> full activity log plus the summary counters
 * shown in the four stat cards at the top of the page.
 */
@WebServlet(name = "TransactionsServlet", urlPatterns = "/transactions")
public class TransactionsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Customer customer = currentCustomer(request);
        if (customer == null) {
            redirect(request, response, "/login");
            return;
        }

        List<TransactionHistory> transactions = customerService.getTransactionHistory(customer);

        long deposits = transactions.stream().filter(TransactionHistory::isDeposit).count();
        long withdrawals = transactions.stream().filter(TransactionHistory::isWithdrawal).count();
        long transfers = transactions.stream().filter(TransactionHistory::isTransfer).count();

        request.setAttribute("customer", customer);
        request.setAttribute("transactions", transactions);
        request.setAttribute("totalCount", transactions.size());
        request.setAttribute("depositCount", deposits);
        request.setAttribute("withdrawalCount", withdrawals);
        request.setAttribute("transferCount", transfers);

        forward(request, response, "transactions.jsp");
    }
}
