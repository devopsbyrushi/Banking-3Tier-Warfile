package com.rushi.securebank.web;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.service.BankingException;
import com.rushi.securebank.service.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * ================================================================
 *  BaseServlet - PRESENTATION TIER
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Small helper superclass so every servlet does not repeat
 *  session lookup, forwarding and amount parsing.
 * ================================================================
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session key holding the logged-in username. */
    public static final String SESSION_USER = "SECUREBANK_USER";

    protected static final String VIEWS = "/WEB-INF/views/";

    protected final transient CustomerService customerService = CustomerService.getInstance();

    /** Returns the logged-in customer, or null when there is no valid session. */
    protected Customer currentCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object username = session.getAttribute(SESSION_USER);
        if (username == null) {
            return null;
        }
        try {
            return customerService.findCustomerByUsername(username.toString());
        } catch (BankingException e) {
            // Account vanished (e.g. after a restart wiped memory) - drop the session.
            session.invalidate();
            return null;
        }
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher(VIEWS + view).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * Parses a money amount from a request parameter.
     *
     * @throws BankingException when the value is missing or not a number
     */
    protected BigDecimal amountParam(HttpServletRequest request, String name) {
        String raw = request.getParameter(name);
        if (raw == null || raw.trim().isEmpty()) {
            throw new BankingException("Please enter an amount");
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new BankingException("Please enter a valid amount");
        }
    }
}
