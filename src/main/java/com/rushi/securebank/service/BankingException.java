package com.rushi.securebank.service;

/**
 * Thrown when a banking business rule is violated
 * (insufficient funds, duplicate username, unknown recipient, ...).
 * The message is safe to display back to the customer.
 */
public class BankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BankingException(String message) {
        super(message);
    }
}
