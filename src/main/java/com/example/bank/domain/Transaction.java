package com.example.bank.domain;
import com.example.bank.domain.TransactionType;

import java.util.UUID;
import java.util.Objects;
import java.time.Instant;
import java.time.LocalDate;

public class Transaction {
    private final UUID id;
    private final Instant timestamp;
    private final LocalDate businessDate;
    private final TransactionType type;
    private final UUID fromAccountId;
    private final UUID toAccountId;
    private final double amount;  
    private final String description;

    public Transaction(UUID id, 
                        Instant timestamp, 
                        LocalDate businessDate, 
                        TransactionType type,
                        UUID fromAccountId, 
                        UUID toAccountId, 
                        double amount, 
                        String description) {
        this.id = Objects.requireNonNull(id, "Transaction ID cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.businessDate = Objects.requireNonNull(businessDate, "Business date cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.fromAccountId = fromAccountId; // can be null for deposits
        this.toAccountId = toAccountId;     // can be null for withdrawals
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        this.amount = amount;
        this.description = description != null ? description : "";
    }

    public UUID getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public TransactionType getType() {
        return type;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public UUID getToAccountId() {
        return toAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
