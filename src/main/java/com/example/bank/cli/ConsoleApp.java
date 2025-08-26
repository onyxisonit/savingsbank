package com.example.bank.cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import com.example.bank.domain.AccountType;
import com.example.bank.domain.Customer;
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
        String name = readNonEmpty("Enter customer name: ");
        String email = readNonEmptyEmail("Enter customer email: ");

        var customer = repo.addCustomer(name.trim(), email.trim());
        System.out.println("Customer created with ID: " + customer.getId());
    }

    private void createAccount() {
        var customerId = pickCustomer();
        var accountType = pickAccountType();
        var initialBalance = readPositiveMoney("Enter initial balance: ");

        var account = accounts.createAccount(customerId.getId(), accountType, initialBalance);
        System.out.println("Account created with ID: " + account.getId());
    }

    private void deposit() {
        var accountId = pickAccount();
        var amount = readPositiveMoney("Enter deposit amount: "); 
        var description = readNonEmpty("Enter description: ");

        accounts.deposit(accountId, amount, description);
        System.out.println("Deposit successful.");
    }

    private void transfer() {
        System.out.print("Choose FROM Account:  ");
        var fromAccountId = pickAccount();
        System.out.print("Choose TO Account:  ");
        var toAccountId = pickAccount();
        if (fromAccountId.equals(toAccountId)) {
            System.out.println("Error: Cannot transfer to the same account.");
            return;
        }
        var amount = readPositiveMoney("Enter transfer amount: "); 
        var description = readNonEmpty("Enter description: ");

        transfers.transfer(fromAccountId, toAccountId, amount, description);
        System.out.println("Transfer successful.");
    }

    private void payment() {
        System.out.print("Choose FROM Account:  ");
        var fromAccountId = pickAccount();
        var amount = readPositiveMoney("Enter transfer amount: "); 
        var description = readNonEmpty("Enter description: ");

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

    //Helper methods for input handling and selection
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (!input.trim().isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private String readNonEmptyEmail(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (!input.trim().isEmpty() && input.contains("@") && input.contains(".")) {
                return input;
            }
            System.out.println("Invalid email. Please try again.");
        }
    }

    private BigDecimal readPositiveMoney(String prompt) {
        while (true) {
            String input = readNonEmpty(prompt);
            try {
                BigDecimal amount = new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    return amount;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid amount. Please enter a positive number.");
        }
    }

    private Customer pickCustomer() {
        var customers = new ArrayList<>(repo.getAllCustomers());
        if (customers.isEmpty()) {
            throw new IllegalStateException("No customers available. Please create a customer first.");
        }
        System.out.println("Available Customers:");
        for (int i = 0; i < customers.size(); i++) {
            System.out.printf("%d. %s (ID: %s)%n", i + 1, customers.get(i).getName(), customers.get(i).getId());
        }
        while (true) {
            String input = readLine("Select customer by number: ");
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < customers.size()) {
                    return customers.get(index);
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid selection. Please try again.");
        }
    }

    private AccountType pickAccountType() {
        var types = AccountType.values();
        System.out.println("Available Account Types:");
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d. %s%n", i + 1, types[i]);
        }
        while (true) {
            String input = readLine("Select account type by number: ");
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < types.length) {
                    return types[index];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid selection. Please try again.");
        }
    }
    
    private UUID pickAccount() {
        var accountsList = new ArrayList<>(repo.getAllAccounts());
        if (accountsList.isEmpty()) {
            throw new IllegalStateException("No accounts available. Please create an account first.");
        }
        System.out.println("Available Accounts:");
        for (int i = 0; i < accountsList.size(); i++) {
            System.out.printf("%d. %s (ID: %s, Balance: %s)%n", i + 1, accountsList.get(i).getCustomerId(), accountsList.get(i).getAccountType(), accountsList.get(i).getId(), accountsList.get(i).getBalance());
        }
        while (true) {
            String input = readLine("Select account by number: ");
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < accountsList.size()) {
                    return accountsList.get(index).getId();
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid selection. Please try again.");
        }
    }
    
}
