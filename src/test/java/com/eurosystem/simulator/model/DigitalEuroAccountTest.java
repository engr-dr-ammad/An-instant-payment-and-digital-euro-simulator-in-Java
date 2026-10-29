package com.eurosystem.simulator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DigitalEuroAccountTest {
    private DigitalEuroAccount account;

    @BeforeEach
    void setUp() {
        account = new DigitalEuroAccount("ACC001", "PERSON001",
                                        new BigDecimal("1000.00"),
                                        new BigDecimal("2000.00"),
                                        "BANK001");
    }

    @Test
    void testAccountCreation() {
        assertEquals("ACC001", account.getAccountId());
        assertEquals("PERSON001", account.getPersonId());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("2000.00"), account.getHoldingLimit());
        assertEquals("BANK001", account.getLinkedBankAccount());
    }

    @Test
    void testCanHoldAmount_WithinLimit() {
        assertTrue(account.canHold(new BigDecimal("500.00")));
    }

    @Test
    void testCanHoldAmount_AtExactLimit() {
        assertTrue(account.canHold(new BigDecimal("1000.00")));
    }

    @Test
    void testGetExcessAmount_NoExcess() {
        assertEquals(BigDecimal.ZERO, account.getExcessAmount());
    }

    @Test
    void testGetExcessAmount_WithExcess() {
        account.setBalance(new BigDecimal("2500.00"));
        assertEquals(new BigDecimal("500.00"), account.getExcessAmount());
    }

    @Test
    void testSetBalance() {
        BigDecimal newBalance = new BigDecimal("3000.00");
        account.setBalance(newBalance);
        assertEquals(newBalance, account.getBalance());
    }

    @Test
    void testSetHoldingLimit() {
        BigDecimal newLimit = new BigDecimal("5000.00");
        account.setHoldingLimit(newLimit);
        assertEquals(newLimit, account.getHoldingLimit());
    }

    @Test
    void testSetLinkedBankAccount() {
        String newBankAccount = "BANK002";
        account.setLinkedBankAccount(newBankAccount);
        assertEquals(newBankAccount, account.getLinkedBankAccount());
    }

    @Test
    void testEdgeCaseCanHold_AtExactLimit() {
        account.setBalance(new BigDecimal("0.00"));
        assertTrue(account.canHold(new BigDecimal("2000.00")));
    }

    @Test
    void testLargeFunds() {
        DigitalEuroAccount richAccount = new DigitalEuroAccount("ACC002", "PERSON002",
                                                                new BigDecimal("100000.00"),
                                                                new BigDecimal("500000.00"),
                                                                "BANK002");

        assertTrue(richAccount.canHold(new BigDecimal("100000.00")));
        assertEquals(BigDecimal.ZERO, richAccount.getExcessAmount());
    }
}
