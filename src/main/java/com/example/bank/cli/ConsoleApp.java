package com.example.bank.cli;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.UUID;

import com.example.bank.domain.AccountType;
import com.example.bank.repository.BankRepository;
import com.example.bank.service.AccountService;
import com.example.bank.service.PaymentService;
import com.example.bank.service.ReportService;
import com.example.bank.service.TransferService;

public class ConsoleApp {
    private final BankRepository repo;
    private final AccountService accounts;
    private final TransferService transfers;
    private final PaymentService payments;
    private final ReportService reports;

    private final Scanner scanner = new Scanner(System.in); //read from stdin

    public ConsoleApp(BankRepository repo, 
                    AccountService accounts, 
                    TransferService transfers, 
                    PaymentService payments, 
                    ReportService reports) {
        this.repo = repo;
        this.accounts = accounts;
        this.transfers = transfers;
        this.payments = payments;
        this.reports = reports;
    }

    public void run() {
        System.out.println("Welcome to the Bank Application!");
        while (true) {
            System.out.println("\n--- Bank Application Menu ---");
            System.out.println("1. Create Customer");
            System.out.println("2. Create Account");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Payment");
            System.out.println("6. Generate Report");
            System.out.println("7. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> createCustomer();
                    case "2" -> createAccount();
                    case "3" -> deposit();
                    case "4" -> transfer();
                    case "5" -> payment();
                    case "6" -> generateReport();
                    case "7" -> {
                        System.out.println("Exiting application.");
                        return;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void createCustomer() {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter customer email: ");
        String email = scanner.nextLine();

        var customer = repo.addCustomer(name, email);
        System.out.println("Customer created with ID: " + customer.getId());
    }

    private void createAccount() {
        System.out.print("Enter customer ID: ");
        String customerIdStr = scanner.nextLine();
        System.out.print("Enter account type (CHECKING/SAVINGS): ");
        String accountTypeStr = scanner.nextLine();
        System.out.print("Enter initial balance: ");
        String initialBalanceStr = scanner.nextLine();

        var customerId = UUID.fromString(customerIdStr);
        var accountType = AccountType.valueOf(accountTypeStr.toUpperCase());
        var initialBalance = new BigDecimal(initialBalanceStr);

        var account = accounts.createAccount(customerId, accountType, initialBalance);
        System.out.println("Account created with ID: " + account.getId());
    }

    private void deposit() {
        System.out.print("Enter account ID: ");
        String accountIdStr = scanner.nextLine();
        System.out.print("Enter deposit amount: ");
        String amountStr = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        var accountId = UUID.fromString(accountIdStr);
        var amount = new BigDecimal(amountStr);

        accounts.deposit(accountId, amount, description);
        System.out.println("Deposit successful.");
    }

    private void transfer() {
        System.out.print("Enter from account ID: ");
        String fromAccountIdStr = scanner.nextLine();
        System.out.print("Enter to account ID: ");
        String toAccountIdStr = scanner.nextLine();
        System.out.print("Enter transfer amount: ");
        String amountStr = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        var fromAccountId = UUID.fromString(fromAccountIdStr);
        var toAccountId = UUID.fromString(toAccountIdStr);
        var amount = new BigDecimal(amountStr);

        transfers.transfer(fromAccountId, toAccountId, amount, description);
        System.out.println("Transfer successful.");
    }

    private void payment() {
        System.out.print("Enter from account ID: ");
        String fromAccountIdStr = scanner.nextLine();
        System.out.print("Enter payment amount: ");
        String amountStr = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        var fromAccountId = UUID.fromString(fromAccountIdStr);
        var amount = new BigDecimal(amountStr);

        payments.pay(fromAccountId, amount, description);
        System.out.println("Payment successful.");
    }

    private void generateReport() {
        System.out.println("Generating report...");
        var report = reports.generateBankReport(5, java.time.Duration.ofDays(30));
        for (var entry : report.balanceByCustomer().entrySet()) {
            System.out.printf("Customer: %s, Total Balance: %s%n", entry.getKey().getName(), entry.getValue());
        }
    }
    
}
