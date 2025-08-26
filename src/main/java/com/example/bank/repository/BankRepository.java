package com.example.bank.repository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.example.bank.domain.Customer;
import com.example.bank.domain.Account;
import com.example.bank.domain.AccountType;
import com.example.bank.domain.Transaction;
import com.example.bank.domain.TransactionType;

public class BankRepository {

    private final Map<UUID, Customer> customers = new ConcurrentHashMap<>();
    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();
    private final Deque<Transaction> transactions = new ConcurrentLinkedDeque<>(); //newest first 

    private final Clock clock;
    private final ZoneId businessZone;
    //dependency injection for testability, default to system UTC for quick start
    public BankRepository(Clock clock, ZoneId businessZone) {
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
        this.businessZone = Objects.requireNonNull(businessZone, "Business zone cannot be null");
    }

    public BankRepository() {
        this(Clock.systemUTC(), ZoneId.of("UTC"));
    }

    //Customer operations
    public Customer addCustomer(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email cannot be null or blank");
        }
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, name, email);
        customers.put(id, customer);
        return customer;
    }

    public Customer getCustomer(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        return customers.get(customerId);
    }

    public Collection<Customer> getAllCustomers() {
        return customers.values();
    }

    //Account operations
    public Account addAccount(UUID customerId, AccountType accountType, BigDecimal initialBalance) {
        if (customerId == null || !customers.containsKey(customerId)) {
            throw new IllegalArgumentException("Invalid customer ID");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Account type cannot be null or blank");
        }
        
        UUID id = UUID.randomUUID();
        Account account = new Account(id, customerId, accountType, initialBalance);
        accounts.put(id, account);

        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            //create an initial deposit transaction if initial balance > 0
            Instant now = Instant.now(clock);
            LocalDate businessDate = now.atZone(businessZone).toLocalDate();
            addTransaction(new Transaction(UUID.randomUUID(), 
                                            now, 
                                            businessDate, 
                                            TransactionType.DEPOSIT, 
                                            null, 
                                            id, 
                                            initialBalance, 
                                            "Initial deposit"));
        }
        return account;
    }

    public Account getAccount(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return accounts.get(accountId);
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public Collection<Account> getAccountsByCustomer(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        return accounts.values().stream()
                .filter(acc -> acc.getCustomerId().equals(customerId))
                .toList();
    }

    //Transaction operations
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        transactions.addFirst(transaction); //newest first
    }

    public List<Transaction> getRecentTransactions(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of transactions must be positive");
        }
        return transactions.stream().limit(n).toList();
    }

    public List<Transaction> getTransactionsSince(Instant since) {
        if (since == null) {
            throw new IllegalArgumentException("Since timestamp cannot be null");
        }
        return transactions.stream()
                .filter(tx -> tx.getTimestamp().isAfter(since))
                .toList();
    }
    
    public List<Transaction> getAllTransactions() {
        return List.copyOf(transactions);
    }

    public List<Transaction> getTransactionsByAccount(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return transactions.stream()
                .filter(tx -> accountId.equals(tx.getFromAccountId()) || accountId.equals(tx.getToAccountId()))
                .toList();
    }
}
