package com.example.bank;

import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import com.example.bank.repository.BankRepository;
import com.example.bank.service.AccountService;
import com.example.bank.service.PaymentService;
import com.example.bank.service.ReportService;
import com.example.bank.service.TransferService;
import com.example.bank.domain.Customer;
import java.math.BigDecimal;


public abstract class TestSetup {
    protected Clock clock;
    protected ZoneId businessZone;

    protected BankRepository repo;
    protected AccountService accountService;
    protected ReportService reportService;
    protected PaymentService paymentService;
    protected TransferService transferService;

    protected Customer alice;
    protected Customer bob;

    protected static BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    @BeforeEach
    public void setup() {
       Clock clock = Clock.fixed(Instant.parse("2023-01-01T10:00:00Z"), ZoneId.of("UTC"));
       ZoneId businessZone = ZoneId.of("America/New_York");

       repo = new BankRepository(clock, businessZone);
       accountService = new AccountService(repo, clock, businessZone);
       reportService = new ReportService(repo);   
       paymentService = new PaymentService(repo, clock, businessZone);
       transferService = new TransferService(repo, clock, businessZone);

        alice = repo.addCustomer("Alice", "alice@email.com");
        bob = repo.addCustomer("Bob", "bob@email.com");

    }
}