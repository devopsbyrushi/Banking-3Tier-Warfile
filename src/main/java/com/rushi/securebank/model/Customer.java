package com.rushi.securebank.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ================================================================
 *  Customer Entity - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Maps to the 'customers' table in securebank_db.
 *  Implements Spring Security's UserDetails for authentication.
 *
 *  Improvements over the trainer's basic example:
 *   - Renamed from 'Account' → 'Customer' (real banks have customers)
 *   - Added 'accountNumber' field (e.g. "SB202600001")
 *   - Added 'accountType' field (SAVINGS / CURRENT)
 *   - Mapped to 'customers' table (was generic 'account')
 * ================================================================
 */
@Entity
@Table(name = "customers")
public class Customer implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @OneToMany(mappedBy = "customer")
    private List<TransactionHistory> transactions;

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    public Customer() {
    }

    public Customer(String username, String password, String accountNumber, String accountType,
                    BigDecimal balance, List<TransactionHistory> transactions,
                    Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.transactions = transactions;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public List<TransactionHistory> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionHistory> transactions) { this.transactions = transactions; }
}
