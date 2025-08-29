package com.example.bank;

import org.junit.jupiter.api.Test;

import com.example.bank.domain.Account;
import com.example.bank.domain.AccountType;
import com.example.bank.domain.Customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountSetup extends TestSetup{
    @Test
    public void accountCreatedandAddedSuccessfully() {
        Account a = repo.addAccount(alice.getId(), AccountType.SAVINGS, bd("1000.00"));
        assertNotNull(a.getId(), "ID should not be null");
        assertEquals(a.getAccountType(), AccountType.SAVINGS, "Account type should be SAVINGS");
        assertEquals(a.getBalance(), bd("1000.00"), "Balance should be 1000.00");
        assertEquals(a.getCustomerId(), alice.getId(), "Customer ID should match Alice's");
    }

    @Test
    public void multipleAccountsForCustomer() {
        Account a1 = repo.addAccount(bob.getId(), AccountType.CHECKING, bd("500.00"));
        Account a2 = repo.addAccount(bob.getId(), AccountType.SAVINGS, bd("1500.00"));

        assertNotNull(a1.getId(), "First account ID should not be null");
        assertNotNull(a2.getId(), "Second account ID should not be null");
        assertEquals(a1.getCustomerId(), bob.getId(), "First account's customer ID should match Bob's");
        assertEquals(a2.getCustomerId(), bob.getId(), "Second account's customer ID should match Bob's");
    }

    @Test
    public void fetchAccountByCustomerId() {
        Customer t = repo.addCustomer("Temp", "temp@email.com");
        Account aT = repo.addAccount(t.getId(), AccountType.CHECKING, bd("750.00"));
        Account aT2 = repo.addAccount(t.getId(), AccountType.SAVINGS, bd("1250.00"));

        var accounts = repo.getAccountsByCustomer(t.getId());

        assertEquals(2, accounts.size(), "Should fetch two accounts for Temp");
        assertTrue(accounts.stream().anyMatch(a -> a.getId().equals(aT.getId())), "Should contain first account");
        assertTrue(accounts.stream().anyMatch(a -> a.getId().equals(aT2.getId())), "Should contain second account");
    }
}
