/*
Single account operations service
  - create account
  - deposit
  - withdraw

    Each operation that modifies account balance must:
        - validate input
        - lock per account
        - perform the operation
        - record a transaction
        - unlock the account
*/
package com.example.bank.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import com.example.bank.domain.Account;
import com.example.bank.domain.AccountType;
import com.example.bank.domain.Customer;
import com.example.bank.domain.Transaction;
import com.example.bank.domain.TransactionType;
import com.example.bank.repository.BankRepository;

public class AccountService {
    private final BankRepository repo;
    private final Clock clock;
    private final ZoneId businessZone;

    public AccountService(BankRepository repo, Clock clock, ZoneId businessZone) {
        this.repo = Objects.requireNonNull(repo, "BankRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
        this.businessZone = Objects.requireNonNull(businessZone, "Business zone cannot be null");
    }

    public Account createAccount(UUID customerId, AccountType accountType, BigDecimal initialBalance) {
        // Ensure customer exists
        Customer customer = repo.getCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer does not exist");
        }

        // Create and store the account
        return repo.addAccount(customerId, accountType, initialBalance);
    }

    public void deposit(UUID accountId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Account account = repo.getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist");
        }

        // Lock the account for thread-safe operation
        account.getLock().lock();
        try {
            account.deposit(amount);

            Instant now = Instant.now(clock);
            LocalDate businessDate = LocalDate.now(businessZone);
            // Record the transaction
            repo.addTransaction(new Transaction(
                                    UUID.randomUUID(), now, businessDate,
                                    TransactionType.DEPOSIT, null, 
                                    accountId, amount, description));
        } finally {
            account.getLock().unlock();
        }
    }

    public void withdraw(UUID accountId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        Account account = repo.getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist");
        }

        // Lock the account for thread-safe operation
        account.getLock().lock();
        try {
            account.withdraw(amount);

            Instant now = Instant.now(clock);
            LocalDate businessDate = LocalDate.now(businessZone);
            // Record the transaction
            repo.addTransaction(new Transaction(
                                    UUID.randomUUID(), now, businessDate,
                                    TransactionType.WITHDRAWAL, 
                                    accountId, null, 
                                    amount, description));
        } finally {
            account.getLock().unlock();
        }
    }
}