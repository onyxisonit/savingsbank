package com.example.bank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.bank.domain.Account;
import com.example.bank.domain.Customer;

public record BankReport(
    BigDecimal totalBalance,
    Map<Customer, BigDecimal> balanceByCustomer,
    int recentTransactionCount,
    List<Account> topAccountsByBalance
){}
