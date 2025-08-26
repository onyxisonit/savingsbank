/*
Two accounts transfer service 
    - validate inputs
    - fetch accounts
    - lock both accounts (deterministic lock ordering: always lock the lower UUID first to prevent deadlocks)
        - check sufficient funds
        - perform the transfer
        - record a transaction
    - unlock both accounts
Logs one TRANSFER transaction
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

public class TransferService {
    private final BankRepository repo;
    public final Clock clock;
    public final ZoneId businessZone;

    public TransferService(BankRepository repo, Clock clock, ZoneId businessZone) {
        this.repo = Objects.requireNonNull(repo, "BankRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
        this.businessZone = Objects.requireNonNull(businessZone, "Business zone cannot be null");
    }

    public void transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description) {
        // Validate inputs
        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Account IDs cannot be null");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        // Fetch accounts
        Account fromAccount = repo.getAccount(fromAccountId);
        Account toAccount = repo.getAccount(toAccountId);
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("One or both accounts do not exist");
        }

        // Lock both accounts to prevent deadlocks by always locking the lower UUID first
        Account firstLock = fromAccountId.compareTo(toAccountId) < 0 ? fromAccount : toAccount;
        Account secondLock = fromAccountId.compareTo(toAccountId) < 0 ? toAccount : fromAccount;

        firstLock.getLock().lock();
        try {
            secondLock.getLock().lock();
            try {
                // Check sufficient funds
                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient funds in the source account");
                }
                // Perform the transfer
                fromAccount.withdraw(amount);
                toAccount.deposit(amount);

                // Record the transaction
                Instant now = Instant.now(clock);
                LocalDate businessDate = now.atZone(businessZone).toLocalDate();

                repo.addTransaction(new Transaction(
                        UUID.randomUUID(), now, businessDate,
                        TransactionType.TRANSFER,
                        fromAccountId, toAccountId, amount, description));
            } finally {
                secondLock.getLock().unlock();
            }
        } finally {
            firstLock.getLock().unlock();
        }
    }
}
