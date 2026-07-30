package com.rushi.securebank.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * ================================================================
 *  Customer - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Plain Java object (POJO). No JPA annotations, no Spring
 *  Security UserDetails - this version stores customers in
 *  memory, so the class is pure data plus a few display helpers.
 *
 *  Display helpers exist so the JSP layer stays free of
 *  formatting logic (JSTL cannot format LocalDateTime directly).
 * ================================================================
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String passwordHash;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance = BigDecimal.ZERO;

    public Customer() {
    }

    public Customer(Long id, String username, String passwordHash,
                    String accountNumber, String accountType, BigDecimal balance) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    /* ---------------- Display helpers used by the JSP views ---------------- */

    /** Balance rendered as 1,23,456.00 style grouping, e.g. "12,500.00". */
    public String getFormattedBalance() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(balance == null ? BigDecimal.ZERO : balance);
    }

    /** First letter of the username, used for the avatar circle. */
    public String getInitial() {
        if (username == null || username.isEmpty()) {
            return "U";
        }
        return username.substring(0, 1).toUpperCase(Locale.ENGLISH);
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", username='" + username
                + "', accountNumber='" + accountNumber + "', balance=" + balance + '}';
    }
}
