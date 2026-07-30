package com.rushi.securebank.store;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.model.TransactionHistory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ================================================================
 *  InMemoryBankStore - DATA TIER
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  This class replaces MySQL + Spring Data JPA repositories.
 *  Everything lives in ConcurrentHashMaps inside the JVM.
 *
 *  ****************************************************************
 *  IMPORTANT - READ BEFORE DEMOING
 *  ****************************************************************
 *  1. All data is LOST when Tomcat restarts or the container is
 *     recreated. This is expected for a no-database build.
 *  2. Each JVM has its OWN copy of the data. If you run more than
 *     one replica in Kubernetes, a customer who registers on pod A
 *     will not exist on pod B. Keep replicas: 1, or enable session
 *     affinity, for a coherent demo.
 *  ****************************************************************
 *
 *  Singleton because a servlet container may create several
 *  instances of the same servlet class, and they must all share
 *  one set of accounts.
 * ================================================================
 */
public final class InMemoryBankStore {

    private static final InMemoryBankStore INSTANCE = new InMemoryBankStore();

    /** username (lower-cased) -> Customer */
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    /** customerId -> that customer's transaction log */
    private final Map<Long, List<TransactionHistory>> transactions = new ConcurrentHashMap<>();

    private final AtomicLong customerIdSequence = new AtomicLong(0);
    private final AtomicLong transactionIdSequence = new AtomicLong(0);

    private InMemoryBankStore() {
    }

    public static InMemoryBankStore getInstance() {
        return INSTANCE;
    }

    /* ------------------------------ Customers ------------------------------ */

    public Optional<Customer> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customers.get(key(username)));
    }

    public boolean existsByUsername(String username) {
        return username != null && customers.containsKey(key(username));
    }

    public boolean accountNumberExists(String accountNumber) {
        return customers.values().stream()
                .anyMatch(c -> c.getAccountNumber().equals(accountNumber));
    }

    /** Inserts a new customer and assigns the next id. */
    public Customer insert(Customer customer) {
        customer.setId(customerIdSequence.incrementAndGet());
        customers.put(key(customer.getUsername()), customer);
        transactions.put(customer.getId(), new CopyOnWriteArrayList<>());
        return customer;
    }

    /** Updates an existing customer (balance changes). */
    public Customer save(Customer customer) {
        customers.put(key(customer.getUsername()), customer);
        return customer;
    }

    public int customerCount() {
        return customers.size();
    }

    /* ---------------------------- Transactions ----------------------------- */

    public TransactionHistory addTransaction(TransactionHistory transaction) {
        transaction.setId(transactionIdSequence.incrementAndGet());
        transactions
                .computeIfAbsent(transaction.getCustomerId(), k -> new CopyOnWriteArrayList<>())
                .add(transaction);
        return transaction;
    }

    /** Returns the customer's transactions, newest first. */
    public List<TransactionHistory> findByCustomerIdNewestFirst(Long customerId) {
        List<TransactionHistory> list = transactions.get(customerId);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<TransactionHistory> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(TransactionHistory::getTransactionDate).reversed()
                .thenComparing(Comparator.comparing(TransactionHistory::getId).reversed()));
        return copy;
    }

    /** Wipes everything. Used by unit tests. */
    public void clear() {
        customers.clear();
        transactions.clear();
        customerIdSequence.set(0);
        transactionIdSequence.set(0);
    }

    private String key(String username) {
        return username.trim().toLowerCase();
    }
}
