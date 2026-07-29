package com.rushi.securebank.service;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.model.TransactionHistory;
import com.rushi.securebank.repository.CustomerRepository;
import com.rushi.securebank.repository.TransactionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ================================================================
 *  CustomerService - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Replaces the trainer's AccountService with clearer naming and
 *  adds a few real-banking touches:
 *   - Auto-generates a unique account number on registration
 *     (e.g., "SB202600042")
 *   - Stores account type (default: SAVINGS)
 *   - Records transaction descriptions (not just type)
 *   - Returns transactions newest-first
 * ================================================================
 */
@Service
public class CustomerService implements UserDetailsService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionHistoryRepository transactionRepository;

    public Customer findCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer registerCustomer(String username, String password) {
        if (customerRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setAccountNumber(generateAccountNumber());
        customer.setAccountType("SAVINGS");
        customer.setBalance(BigDecimal.ZERO);
        return customerRepository.save(customer);
    }

    /**
     * Generates a unique 11-character account number, e.g. "SB202600042"
     *   SB    = SecureBank prefix
     *   2026  = current year
     *   00042 = 5-digit random number
     */
    private String generateAccountNumber() {
        int randomDigits = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "SB" + Year.now().getValue() + randomDigits;
    }

    public void deposit(Customer customer, BigDecimal amount) {
        customer.setBalance(customer.getBalance().add(amount));
        customerRepository.save(customer);

        TransactionHistory transaction = new TransactionHistory(
                amount,
                "DEPOSIT",
                "Cash deposit to account",
                LocalDateTime.now(),
                customer
        );
        transactionRepository.save(transaction);
    }

    public void withdraw(Customer customer, BigDecimal amount) {
        if (customer.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in your account");
        }
        customer.setBalance(customer.getBalance().subtract(amount));
        customerRepository.save(customer);

        TransactionHistory transaction = new TransactionHistory(
                amount,
                "WITHDRAWAL",
                "Cash withdrawal from account",
                LocalDateTime.now(),
                customer
        );
        transactionRepository.save(transaction);
    }

    public List<TransactionHistory> getTransactionHistory(Customer customer) {
        return transactionRepository.findByCustomerIdOrderByDateDesc(customer.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = findCustomerByUsername(username);
        if (customer == null) {
            throw new UsernameNotFoundException("Username or Password not found");
        }
        return new Customer(
                customer.getUsername(),
                customer.getPassword(),
                customer.getAccountNumber(),
                customer.getAccountType(),
                customer.getBalance(),
                customer.getTransactions(),
                authorities());
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return Arrays.asList(new SimpleGrantedAuthority("USER"));
    }

    public void transferAmount(Customer fromCustomer, String toUsername, BigDecimal amount) {
        if (fromCustomer.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in your account");
        }

        Customer toCustomer = customerRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        // Deduct from sender's account
        fromCustomer.setBalance(fromCustomer.getBalance().subtract(amount));
        customerRepository.save(fromCustomer);

        // Add to recipient's account
        toCustomer.setBalance(toCustomer.getBalance().add(amount));
        customerRepository.save(toCustomer);

        // Create transaction records for both customers
        TransactionHistory debit = new TransactionHistory(
                amount,
                "TRANSFER_OUT",
                "Transfer to " + toCustomer.getUsername() + " (A/C " + toCustomer.getAccountNumber() + ")",
                LocalDateTime.now(),
                fromCustomer
        );
        transactionRepository.save(debit);

        TransactionHistory credit = new TransactionHistory(
                amount,
                "TRANSFER_IN",
                "Received from " + fromCustomer.getUsername() + " (A/C " + fromCustomer.getAccountNumber() + ")",
                LocalDateTime.now(),
                toCustomer
        );
        transactionRepository.save(credit);
    }
}
