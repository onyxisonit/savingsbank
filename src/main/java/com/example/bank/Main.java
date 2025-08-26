package com.example.bank;

import java.time.Clock;
import java.time.ZoneId;

import com.example.bank.cli.ConsoleApp;
import com.example.bank.repository.BankRepository;
import com.example.bank.service.AccountService;
import com.example.bank.service.PaymentService;
import com.example.bank.service.ReportService;
import com.example.bank.service.TransferService;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Bank Application!");

        Clock clock = Clock.systemUTC();
        ZoneId businessZone = ZoneId.of("America/New_York");

        BankRepository repo = new BankRepository(clock, businessZone);

        AccountService accountService = new AccountService(repo, clock, businessZone);
        TransferService transferService = new TransferService(repo, clock, businessZone);
        PaymentService paymentService = new PaymentService(repo, clock, businessZone);
        ReportService reports = new ReportService(repo);

        new ConsoleApp(repo, accountService, transferService, paymentService, reports).run();
    }
}