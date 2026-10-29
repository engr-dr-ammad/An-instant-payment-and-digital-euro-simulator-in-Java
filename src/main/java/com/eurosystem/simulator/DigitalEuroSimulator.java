package com.eurosystem.simulator;

import com.eurosystem.simulator.model.DigitalEuroAccount;
import com.eurosystem.simulator.model.PaymentMessage;
import com.eurosystem.simulator.model.PaymentResult;
import com.eurosystem.simulator.payment.PaymentProcessor;
import com.eurosystem.simulator.pacs.Pacs008Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DigitalEuroSimulator {
    private static final Logger logger = LoggerFactory.getLogger(DigitalEuroSimulator.class);
    private final PaymentProcessor processor;

    public DigitalEuroSimulator() {
        this.processor = new PaymentProcessor();
    }

    public void setupDemoAccounts() {
        // Create demo accounts with holding limits
        DigitalEuroAccount account1 = new DigitalEuroAccount(
            "ACC001",
            "PERSON001",
            new BigDecimal("5000.00"),
            new BigDecimal("2000.00"), // 2000 EUR holding limit
            "BANK001-ACC001"
        );

        DigitalEuroAccount account2 = new DigitalEuroAccount(
            "ACC002",
            "PERSON002",
            new BigDecimal("3000.00"),
            new BigDecimal("3000.00"), // 3000 EUR holding limit
            "BANK002-ACC002"
        );

        DigitalEuroAccount account3 = new DigitalEuroAccount(
            "ACC003",
            "PERSON003",
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"), // 1500 EUR holding limit
            "BANK003-ACC003"
        );

        processor.registerAccount(account1);
        processor.registerAccount(account2);
        processor.registerAccount(account3);

        logger.info("Demo accounts setup complete");
    }

    public PaymentResult processPaymentFromXml(String xmlContent) {
        try {
            PaymentMessage message = Pacs008Parser.parseFromXml(xmlContent);
            return processor.processPayment(message);
        } catch (Pacs008Parser.Pacs008ParseException e) {
            logger.error("Failed to parse PACS.008 message: {}", e.getMessage());
            return null;
        }
    }

    public PaymentResult processPaymentFromFile(String filePath) {
        try {
            PaymentMessage message = Pacs008Parser.parseFromFile(Paths.get(filePath));
            return processor.processPayment(message);
        } catch (Exception e) {
            logger.error("Failed to process payment file: {}", e.getMessage());
            return null;
        }
    }

    public PaymentProcessor getProcessor() {
        return processor;
    }

    public void printStatistics() {
        System.out.println("\n=== Simulator Statistics ===");
        System.out.println("Total processed payments: " + processor.getTotalProcessedPayments());
        System.out.println("Total waterfall transactions: " + processor.getWaterfallTransactions().size());

        System.out.println("\n=== Waterfall Transactions ===");
        processor.getWaterfallTransactions().forEach(wt ->
            System.out.println("  " + wt)
        );
    }

    public static void main(String[] args) {
        logger.info("Starting Digital Euro Simulator...");
        DigitalEuroSimulator simulator = new DigitalEuroSimulator();
        simulator.setupDemoAccounts();

        // Example: Process a simple payment
        String samplePacs008 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>MSG001</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>500.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentResult result = simulator.processPaymentFromXml(samplePacs008);
        if (result != null) {
            System.out.println("\nPayment Result: " + result);
        }

        simulator.printStatistics();
        logger.info("Digital Euro Simulator completed");
    }
}
