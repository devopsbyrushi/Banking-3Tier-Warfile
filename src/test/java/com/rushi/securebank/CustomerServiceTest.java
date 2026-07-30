package com.rushi.securebank;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.model.TransactionHistory;
import com.rushi.securebank.service.BankingException;
import com.rushi.securebank.service.CustomerService;
import com.rushi.securebank.store.InMemoryBankStore;
import com.rushi.securebank.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Business rule tests for the no-database SecureBank build.
 * Run with: mvn test
 */
class CustomerServiceTest {

    private final CustomerService service = CustomerService.getInstance();

    @BeforeEach
    void resetStore() {
        InMemoryBankStore.getInstance().clear();
    }

    @Test
    @DisplayName("Registration creates a SAVINGS account with a zero balance")
    void registrationCreatesAccount() {
        Customer customer = service.registerCustomer("rushi", "secret123");

        assertNotNull(customer.getId());
        assertEquals("rushi", customer.getUsername());
        assertEquals("SAVINGS", customer.getAccountType());
        assertEquals(0, customer.getBalance().compareTo(BigDecimal.ZERO));
        assertTrue(customer.getAccountNumber().startsWith("SB"));
    }

    @Test
    @DisplayName("Passwords are never stored in plain text")
    void passwordIsHashed() {
        Customer customer = service.registerCustomer("rushi", "secret123");

        assertFalse(customer.getPasswordHash().contains("secret123"));
        assertTrue(PasswordUtil.matches("secret123", customer.getPasswordHash()));
        assertFalse(PasswordUtil.matches("wrong", customer.getPasswordHash()));
    }

    @Test
    @DisplayName("Duplicate usernames are rejected")
    void duplicateUsernameRejected() {
        service.registerCustomer("rushi", "secret123");

        BankingException error = assertThrows(BankingException.class,
                () -> service.registerCustomer("rushi", "another"));
        assertEquals("Username already exists", error.getMessage());
    }

    @Test
    @DisplayName("Authentication succeeds only with the right password")
    void authentication() {
        service.registerCustomer("rushi", "secret123");

        assertNotNull(service.authenticate("rushi", "secret123"));
        assertNull(service.authenticate("rushi", "bad-password"));
        assertNull(service.authenticate("nobody", "secret123"));
    }

    @Test
    @DisplayName("Deposit increases the balance and logs the transaction")
    void deposit() {
        Customer customer = service.registerCustomer("rushi", "secret123");
        service.deposit(customer, new BigDecimal("5000.00"));

        Customer reloaded = service.findCustomerByUsername("rushi");
        assertEquals(0, reloaded.getBalance().compareTo(new BigDecimal("5000.00")));

        List<TransactionHistory> history = service.getTransactionHistory(reloaded);
        assertEquals(1, history.size());
        assertEquals(TransactionHistory.DEPOSIT, history.get(0).getTransactionType());
    }

    @Test
    @DisplayName("Withdrawal beyond the balance is blocked")
    void insufficientFunds() {
        Customer customer = service.registerCustomer("rushi", "secret123");
        service.deposit(customer, new BigDecimal("100.00"));

        BankingException error = assertThrows(BankingException.class,
                () -> service.withdraw(customer, new BigDecimal("500.00")));
        assertEquals("Insufficient funds in your account", error.getMessage());

        assertEquals(0, service.findCustomerByUsername("rushi")
                .getBalance().compareTo(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Zero and negative amounts are rejected")
    void invalidAmounts() {
        Customer customer = service.registerCustomer("rushi", "secret123");

        assertThrows(BankingException.class, () -> service.deposit(customer, BigDecimal.ZERO));
        assertThrows(BankingException.class,
                () -> service.deposit(customer, new BigDecimal("-50")));
    }

    @Test
    @DisplayName("Transfer moves money and writes two log entries")
    void transfer() {
        Customer sender = service.registerCustomer("rushi", "secret123");
        service.registerCustomer("amit", "secret123");
        service.deposit(sender, new BigDecimal("1000.00"));

        service.transferAmount(sender, "amit", new BigDecimal("250.00"));

        Customer reloadedSender = service.findCustomerByUsername("rushi");
        Customer recipient = service.findCustomerByUsername("amit");

        assertEquals(0, reloadedSender.getBalance().compareTo(new BigDecimal("750.00")));
        assertEquals(0, recipient.getBalance().compareTo(new BigDecimal("250.00")));

        assertEquals(TransactionHistory.TRANSFER_OUT,
                service.getTransactionHistory(reloadedSender).get(0).getTransactionType());
        assertEquals(TransactionHistory.TRANSFER_IN,
                service.getTransactionHistory(recipient).get(0).getTransactionType());
    }

    @Test
    @DisplayName("Transfer to an unknown or self account is rejected")
    void transferValidation() {
        Customer sender = service.registerCustomer("rushi", "secret123");
        service.deposit(sender, new BigDecimal("1000.00"));

        assertThrows(BankingException.class,
                () -> service.transferAmount(sender, "ghost", new BigDecimal("10.00")));
        assertThrows(BankingException.class,
                () -> service.transferAmount(sender, "rushi", new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("Transaction history is returned newest first")
    void historyOrdering() {
        Customer customer = service.registerCustomer("rushi", "secret123");
        service.deposit(customer, new BigDecimal("100.00"));
        service.deposit(customer, new BigDecimal("200.00"));
        service.withdraw(customer, new BigDecimal("50.00"));

        List<TransactionHistory> history =
                service.getTransactionHistory(service.findCustomerByUsername("rushi"));

        assertEquals(3, history.size());
        assertEquals(TransactionHistory.WITHDRAWAL, history.get(0).getTransactionType());
    }
}
