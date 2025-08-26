package com.example.bank.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock; //same thread can acquire the lock multiple times, balance thread needs atomicity

public class Account {
    private final UUID id;
    private final UUID customerId;
    private final AccountType accountType;
    private BigDecimal balance;
    private final ReentrantLock lock = new ReentrantLock(); //manual lock to protect shared, mutable state 

    public Account(UUID id, UUID customerId, AccountType accountType, BigDecimal initialBalance) {
        this.id = Objects.requireNonNull(id, "Account ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.accountType = Objects.requireNonNull(accountType, "Account type cannot be null");
        this.balance = initialBalance == null ? BigDecimal.ZERO : initialBalance;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }
        balance = balance.subtract(amount);
    }
}
