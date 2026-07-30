package com.rushi.securebank.service;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.model.TransactionHistory;
import com.rushi.securebank.store.InMemoryBankStore;
import com.rushi.securebank.util.PasswordUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ================================================================
 *  CustomerService - BUSINESS TIER
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Same responsibilities as the Spring version, but with plain
 *  Java: no @Service, no @Autowired, no @Transactional.
 *
 *  Money-moving operations are guarded by a single lock so that
 *  two concurrent requests cannot corrupt a balance. With a real
 *  database this job belongs to transactions; in memory we do it
 *  ourselves.
 * ================================================================
 */
public class CustomerService {

    private static final CustomerService INSTANCE = new CustomerService();

    /** Guards every read-modify-write of a balance. */
    private final Object moneyLock = new Object();

    private final InMemoryBankStore store = InMemoryBankStore.getInstance();

    private CustomerService() {
    }

    public static CustomerService getInstance() {
        return INSTANCE;
    }

    /* ----------------------------- Accounts ------------------------------- */

    public Customer findCustomerByUsername(String username) {
        return store.findByUsername(username)
                .orElseThrow(() -> new BankingException("Customer not found"));
    }

    public Customer registerCustomer(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new BankingException("Username is required");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new BankingException("Password must be at least 4 characters");
        }

        String cleanUsername = username.trim();

        synchronized (moneyLock) {
            if (store.existsByUsername(cleanUsername)) {
                throw new BankingException("Username already exists");
            }

            Customer customer = new Customer();
            customer.setUsername(cleanUsername);
            customer.setPasswordHash(PasswordUtil.hash(rawPassword));
            customer.setAccountNumber(generateAccountNumber());
            customer.setAccountType("SAVINGS");
            customer.setBalance(BigDecimal.ZERO.setScale(2));
            return store.insert(customer);
        }
    }

    /**
     * Verifies credentials.
     *
     * @return the customer on success, or null when the username is
     *         unknown or the password does not match.
     */
    public Customer authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }
        Customer customer = store.findByUsername(username).orElse(null);
        if (customer == null) {
            return null;
        }
        return PasswordUtil.matches(rawPassword, customer.getPasswordHash()) ? customer : null;
    }

    /**
     * Generates an account number such as "SB202612345".
     *   SB    = SecureBank prefix
     *   2026  = current year
     *   12345 = 5 random digits
     */
    private String generateAccountNumber() {
        for (int attempt = 0; attempt < 50; attempt++) {
            String candidate = "SB" + Year.now().getValue()
                    + ThreadLocalRandom.current().nextInt(10000, 100000);
            if (!store.accountNumberExists(candidate)) {
                return candidate;
            }
        }
        throw new BankingException("Could not allocate an account number, please retry");
    }

    /* ---------------------------- Money movement --------------------------- */

    public void deposit(Customer customer, BigDecimal amount) {
        validateAmount(amount);
        synchronized (moneyLock) {
            Customer current = findCustomerByUsername(customer.getUsername());
            current.setBalance(current.getBalance().add(amount));
            store.save(current);
            record(current, amount, TransactionHistory.DEPOSIT, "Cash deposit to account");
        }
    }

    public void withdraw(Customer customer, BigDecimal amount) {
        validateAmount(amount);
        synchronized (moneyLock) {
            Customer current = findCustomerByUsername(customer.getUsername());
            if (current.getBalance().compareTo(amount) < 0) {
                throw new BankingException("Insufficient funds in your account");
            }
            current.setBalance(current.getBalance().subtract(amount));
            store.save(current);
            record(current, amount, TransactionHistory.WITHDRAWAL, "Cash withdrawal from account");
        }
    }

    public void transferAmount(Customer fromCustomer, String toUsername, BigDecimal amount) {
        validateAmount(amount);
        if (toUsername == null || toUsername.trim().isEmpty()) {
            throw new BankingException("Recipient username is required");
        }

        synchronized (moneyLock) {
            Customer sender = findCustomerByUsername(fromCustomer.getUsername());

            if (sender.getUsername().equalsIgnoreCase(toUsername.trim())) {
                throw new BankingException("You cannot transfer money to your own account");
            }

            Customer recipient = store.findByUsername(toUsername.trim())
                    .orElseThrow(() -> new BankingException("Recipient account not found"));

            if (sender.getBalance().compareTo(amount) < 0) {
                throw new BankingException("Insufficient funds in your account");
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            recipient.setBalance(recipient.getBalance().add(amount));
            store.save(sender);
            store.save(recipient);

            record(sender, amount, TransactionHistory.TRANSFER_OUT,
                    "Transfer to " + recipient.getUsername()
                            + " (A/C " + recipient.getAccountNumber() + ")");

            record(recipient, amount, TransactionHistory.TRANSFER_IN,
                    "Received from " + sender.getUsername()
                            + " (A/C " + sender.getAccountNumber() + ")");
        }
    }

    /* ------------------------------- History ------------------------------- */

    public List<TransactionHistory> getTransactionHistory(Customer customer) {
        return store.findByCustomerIdNewestFirst(customer.getId());
    }

    /* ------------------------------- Helpers ------------------------------- */

    private void record(Customer customer, BigDecimal amount, String type, String description) {
        store.addTransaction(new TransactionHistory(
                null, amount, type, description, LocalDateTime.now(), customer.getId()));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new BankingException("Amount cannot have more than 2 decimal places");
        }
    }
}
