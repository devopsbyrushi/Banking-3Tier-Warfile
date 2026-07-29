package com.rushi.securebank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 *  TransactionHistory Entity - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Maps to the 'transaction_history' table in securebank_db.
 *
 *  Why renamed from 'Transaction' to 'TransactionHistory'?
 *   - 'TRANSACTION' is a reserved SQL keyword in MySQL → causes
 *     issues with raw queries and some ORMs
 *   - 'transaction_history' clearly describes the data: a log
 *     of all past transactions
 *
 *  Improvements over the trainer's basic example:
 *   - Renamed from 'Transaction' → 'TransactionHistory'
 *   - Added 'description' column for transaction notes
 *   - Renamed 'type' → 'transactionType' (clearer)
 *   - Renamed 'timestamp' → 'transactionDate' (more banking-style)
 *   - Foreign key now uses 'customer_id' (was 'account_id')
 * ================================================================
 */
@Entity
@Table(name = "transaction_history")
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public TransactionHistory() {
    }

    public TransactionHistory(BigDecimal amount, String transactionType, String description,
                              LocalDateTime transactionDate, Customer customer) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = transactionDate;
        this.customer = customer;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
