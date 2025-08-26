/*
Report data transfer object for ReportService results
*/
package com.example.bank.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.example.bank.domain.Account;
import com.example.bank.domain.Customer;
import com.example.bank.repository.BankRepository;

public class ReportService {
    private final BankRepository repo;

    public ReportService(BankRepository repo) {
        this.repo = Objects.requireNonNull(repo, "BankRepository cannot be null");
    }

    public BankReport generateBankReport(int topNAccounts, Duration lookback) {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            Callable<BigDecimal> totalBalanceTask = () ->
                repo.getAllAccounts().stream()
                    .map(acc -> acc.getBalance())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Callable<Map<Customer, BigDecimal>> balanceByCustomerTask = () -> {
                Map<UUID, List<Account>> grouped = repo.getAllAccounts().stream()
                    .collect(Collectors.groupingBy(acc -> acc.getCustomerId()));
                Map<Customer, BigDecimal> result = new HashMap<>();
                for (UUID customerId : grouped.keySet().stream().sorted().toList()) {
                    Customer customer = repo.getCustomer(customerId);
                    BigDecimal total = grouped.get(customerId).stream()
                        .map(acc -> acc.getBalance())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.put(customer, total);
                }
                return result;
            };

            Callable<Integer> recentTransactionCountTask = () -> 
                repo.getTransactionsSince(Instant.now().minus(lookback)).size();
            
            Callable<List<Account>> topNAccountsTask = () -> 
                repo.getAllAccounts().stream()
                    .sorted((a, b) -> b.getBalance().compareTo(a.getBalance()))
                    .limit(topNAccounts)
                    .toList();

            Future<BigDecimal> totalBalanceFuture = pool.submit(totalBalanceTask);
            Future<Map<Customer, BigDecimal>> balanceByCustomerFuture = pool.submit(balanceByCustomerTask);
            Future<Integer> recentTransactionCountFuture = pool.submit(recentTransactionCountTask);
            Future<List<Account>> topNAccountsFuture = pool.submit(topNAccountsTask);

            return new BankReport(
                get(totalBalanceFuture),
                get(balanceByCustomerFuture),
                get(recentTransactionCountFuture),
                get(topNAccountsFuture)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate bank report", e);
        } finally {
            pool.shutdown();
        }
    }

    private <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get future result", e);
        } 
    }
}
