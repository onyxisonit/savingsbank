/*
Single account payment service
  - pay from account (to external, not transfer)

    Each operation that modifies account balance:
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
import com.example.bank.domain.Transaction;
import com.example.bank.domain.TransactionType;
import com.example.bank.repository.BankRepository;

public class PaymentService {
    private final BankRepository repo;
    private final Clock clock;
    private final ZoneId businessZone;

    public PaymentService(BankRepository repo, Clock clock, ZoneId businessZone) {
        this.repo = Objects.requireNonNull(repo, "BankRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null"); 
        this.businessZone = Objects.requireNonNull(businessZone, "Business zone cannot be null");
    }

    public void pay(UUID fromAccountId, BigDecimal amount, String description) {
        if (fromAccountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        // Fetch account
        Account fromAccount = repo.getAccount(fromAccountId);
        if (fromAccount == null) {
            throw new IllegalArgumentException("Account does not exist");
        }

        // Lock the account to ensure thread safety
        fromAccount.getLock().lock();
        try {
            // Check sufficient funds
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds in the account");
            }
            // Perform the payment
            fromAccount.withdraw(amount);

            Instant now = Instant.now(clock);
            LocalDate businessDate = LocalDate.now(businessZone);

            // Record the transaction
            repo.addTransaction(new Transaction(
                                    UUID.randomUUID(), now, businessDate,
                                    TransactionType.PAYMENT,
                                    fromAccountId, null, 
                                    amount, description));
        } finally {
            fromAccount.getLock().unlock();
        }
    }
}
