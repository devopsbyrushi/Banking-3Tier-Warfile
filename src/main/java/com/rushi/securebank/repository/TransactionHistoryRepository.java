package com.rushi.securebank.repository;

import com.rushi.securebank.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for TransactionHistory entity.
 * Replaces the trainer's TransactionRepository with cleaner naming
 * and adds ORDER BY DESC for newest-first display.
 */
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    /**
     * Returns transactions for a customer, newest first.
     */
    @Query("SELECT t FROM TransactionHistory t WHERE t.customer.id = :customerId " +
           "ORDER BY t.transactionDate DESC")
    List<TransactionHistory> findByCustomerIdOrderByDateDesc(@Param("customerId") Long customerId);
}
