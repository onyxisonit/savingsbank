package com.example.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.example.bank.domain.Customer;

public class CustomerSetup extends TestSetup {
    @Test
    public void customerCreatedSuccessfully() {
        Customer c = repo.addCustomer("Bob", "bob@email.com");
        assertNotNull(c.getId(), "ID should not be null");
        assertEquals("Bob", c.getName(), "Name should be Bob");
        assertEquals("bob@email.com", c.getEmail(), "Email should be 'bob@email.com'");

        Customer fetched = repo.getCustomer(c.getId());
        assertNotNull(fetched, "Fetched customer should not be null");
        assertEquals(c.getId(), fetched.getId(), "IDs should match");
        assertEquals(c.getName(), fetched.getName(), "Names should match");
        assertEquals(c.getEmail(), fetched.getEmail(), "Emails should match");

        assertEquals(c, fetched, "Fetched customer should match created customer");
    }
}

