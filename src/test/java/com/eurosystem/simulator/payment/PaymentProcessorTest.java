package com.eurosystem.simulator.payment;

import com.eurosystem.simulator.model.DigitalEuroAccount;
import com.eurosystem.simulator.model.PaymentMessage;
import com.eurosystem.simulator.model.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentProcessorTest {
    private PaymentProcessor processor;
    private DigitalEuroAccount account1;
    private DigitalEuroAccount account2;

    @BeforeEach
    void setUp() {
        processor = new PaymentProcessor();
        account1 = new DigitalEuroAccount("ACC001", "PERSON001",
                                         new BigDecimal("5000.00"),
                                         new BigDecimal("2000.00"),
                                         "BANK001");
        account2 = new DigitalEuroAccount("ACC002", "PERSON002",
                                         new BigDecimal("1000.00"),
                                         new BigDecimal("3000.00"),
                                         "BANK002");
        processor.registerAccount(account1);
        processor.registerAccount(account2);
    }

    @Test
    void testSuccessfulPayment() {
        PaymentMessage message = new PaymentMessage(
            "MSG001", "ACC001", "ACC002", new BigDecimal("500.00"), "EUR", "OTHR"
        );

        PaymentResult result = processor.processPayment(message);

        assertTrue(result.isSuccessful());
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(new BigDecimal("4500.00"), account1.getBalance());
        assertEquals(new BigDecimal("1500.00"), account2.getBalance());
    }

    @Test
    void testInsufficientFunds() {
        PaymentMessage message = new PaymentMessage(
            "MSG002", "ACC001", "ACC002", new BigDecimal("6000.00"), "EUR", "OTHR"
        );

        PaymentResult result = processor.processPayment(message);

        assertFalse(result.isSuccessful());
        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getReason().contains("Insufficient"));
    }

    @Test
    void testHoldingLimitExceeded_TriggersWaterfall() {
        PaymentMessage message = new PaymentMessage(
            "MSG003", "ACC001", "ACC002", new BigDecimal("2500.00"), "EUR", "OTHR"
        );

        PaymentResult result = processor.processPayment(message);

        // Payment succeeds but triggers waterfall
        assertTrue(result.isSuccessful());
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(1, processor.getWaterfallTransactions().size());
    }

    @Test
    void testWaterfallTransfer() {
        // This payment will push account2 over its 3000 limit
        PaymentMessage message = new PaymentMessage(
            "MSG004", "ACC001", "ACC002", new BigDecimal("2500.00"), "EUR", "OTHR"
        );

        // Lower the holding limit to trigger waterfall on a smaller amount
        account2.setHoldingLimit(new BigDecimal("2000.00"));

        PaymentMessage validMessage = new PaymentMessage(
            "MSG005", "ACC001", "ACC002", new BigDecimal("1500.00"), "EUR", "OTHR"
        );

        PaymentResult result = processor.processPayment(validMessage);
        assertTrue(result.isSuccessful());

        // Check that waterfall was triggered
        assertEquals(1, processor.getWaterfallTransactions().size());
        assertEquals(new BigDecimal("500.00"),
                    processor.getWaterfallTransactions().get(0).getAmount());
    }

    @Test
    void testInvalidPaymentMessage() {
        PaymentMessage invalidMessage = new PaymentMessage(
            "MSG006", "ACC001", "ACC002", new BigDecimal("-100.00"), "EUR", "OTHR"
        );

        PaymentResult result = processor.processPayment(invalidMessage);

        assertFalse(result.isSuccessful());
    }

    @Test
    void testNonExistentAccount() {
        PaymentMessage message = new PaymentMessage(
            "MSG007", "INVALID_ACC", "ACC002", new BigDecimal("100.00"), "EUR", "OTHR"
        );

        PaymentResult result = processor.processPayment(message);

        assertFalse(result.isSuccessful());
        assertTrue(result.getReason().contains("Invalid payer or payee"));
    }

    @Test
    void testMultiplePayments() {
        PaymentMessage msg1 = new PaymentMessage(
            "MSG008", "ACC001", "ACC002", new BigDecimal("300.00"), "EUR", "OTHR"
        );
        PaymentMessage msg2 = new PaymentMessage(
            "MSG009", "ACC002", "ACC001", new BigDecimal("100.00"), "EUR", "OTHR"
        );

        PaymentResult result1 = processor.processPayment(msg1);
        PaymentResult result2 = processor.processPayment(msg2);

        assertTrue(result1.isSuccessful());
        assertTrue(result2.isSuccessful());
        assertEquals(2, processor.getTotalProcessedPayments());
    }

    @Test
    void testAccountRegistration() {
        DigitalEuroAccount newAccount = new DigitalEuroAccount("ACC003", "PERSON003",
                                                              new BigDecimal("2000.00"),
                                                              new BigDecimal("1000.00"),
                                                              "BANK003");
        processor.registerAccount(newAccount);

        assertNotNull(processor.getAccount("ACC003"));
        assertEquals(newAccount, processor.getAccount("ACC003"));
    }

    @Test
    void testProcessorReset() {
        PaymentMessage message = new PaymentMessage(
            "MSG010", "ACC001", "ACC002", new BigDecimal("100.00"), "EUR", "OTHR"
        );
        processor.processPayment(message);

        assertEquals(1, processor.getTotalProcessedPayments());

        processor.reset();

        assertEquals(0, processor.getTotalProcessedPayments());
        assertNull(processor.getAccount("ACC001"));
    }
}
