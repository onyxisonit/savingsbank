package com.example.bank.domain;

import com.example.bank.domain.AccountType; 
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock; //same thread can acquire the lock multiple times, balance thread needs atomicity

private final AccountType accountType; 

public class Account {
    private final UUID id;
    private final UUID customerId;
    private final String accountType;
    private double balance;
    private final ReentrantLock lock = new ReentrantLock(); //manual lock to protect shared, mutable state 

    public Account(UUID id, UUID customerId, String accountType, double initialBalance) {
        this.id = Objects.requireNonNull(id, "Account ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.accountType = Objects.requireNonNull(accountType, "Account type cannot be null");
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.balance = initialBalance;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance -= amount;
    }
}
