package com.rushi.securebank.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * ================================================================
 *  TransactionHistory - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Plain Java object. Holds one entry of the account activity log.
 *
 *  Transaction types: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
 *
 *  The customer is referenced by id (customerId) rather than by
 *  object reference, which keeps the in-memory store simple and
 *  avoids circular references.
 * ================================================================
 */
public class TransactionHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAWAL = "WITHDRAWAL";
    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);

    private Long id;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private LocalDateTime transactionDate;
    private Long customerId;

    public TransactionHistory() {
    }

    public TransactionHistory(Long id, BigDecimal amount, String transactionType,
                              String description, LocalDateTime transactionDate, Long customerId) {
        this.id = id;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = transactionDate;
        this.customerId = customerId;
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

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    /* ---------------- Display helpers used by the JSP views ---------------- */

    /** Amount rendered as "12,500.00". */
    public String getFormattedAmount() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount == null ? BigDecimal.ZERO : amount);
    }

    /** Date part only, e.g. "30 Jul 2026". */
    public String getDatePart() {
        return transactionDate == null ? "" : transactionDate.format(DATE_FORMAT);
    }

    /** Time part only, e.g. "14:32:07". */
    public String getTimePart() {
        return transactionDate == null ? "" : transactionDate.format(TIME_FORMAT);
    }

    /* Boolean getters below are read from JSP as ${transaction.deposit} etc. */

    public boolean isDeposit() {
        return DEPOSIT.equals(transactionType);
    }

    public boolean isWithdrawal() {
        return WITHDRAWAL.equals(transactionType);
    }

    public boolean isTransfer() {
        return transactionType != null && transactionType.contains("TRANSFER");
    }

    @Override
    public String toString() {
        return "TransactionHistory{id=" + id + ", type='" + transactionType
                + "', amount=" + amount + ", customerId=" + customerId + '}';
    }
}
