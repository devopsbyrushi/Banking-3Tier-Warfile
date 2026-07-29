package com.rushi.securebank.repository;

import com.rushi.securebank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Customer entity.
 * Replaces the trainer's AccountRepository with cleaner naming.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByAccountNumber(String accountNumber);
}
