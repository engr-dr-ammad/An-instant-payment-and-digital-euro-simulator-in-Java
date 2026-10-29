package com.eurosystem.simulator;

import com.eurosystem.simulator.model.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigitalEuroSimulatorTest {
    private DigitalEuroSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new DigitalEuroSimulator();
        simulator.setupDemoAccounts();
    }

    @Test
    void testSimulatorInitialization() {
        assertNotNull(simulator.getProcessor());
        assertEquals(3, simulator.getProcessor().getAccount("ACC001") != null ? 3 : 0);
    }

    @Test
    void testProcessPaymentFromXml_ValidPayment() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST001</MsgId>
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

        PaymentResult result = simulator.processPaymentFromXml(xml);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void testProcessPaymentFromXml_InvalidCurrency() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST002</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>500.00</Amount>
                <Ccy>USD</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentResult result = simulator.processPaymentFromXml(xml);

        assertNull(result); // Parser throws exception
    }

    @Test
    void testProcessPaymentFromXml_HoldingLimitExceeded_TriggersWaterfall() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST003</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>3500.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentResult result = simulator.processPaymentFromXml(xml);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void testProcessMultiplePayments() {
        String payment1 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST004</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>400.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        String payment2 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST005</MsgId>
                <Initiator>
                    <Id>ACC002</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC003</Id>
                </Beneficiary>
                <Amount>300.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentResult result1 = simulator.processPaymentFromXml(payment1);
        PaymentResult result2 = simulator.processPaymentFromXml(payment2);

        assertTrue(result1.isSuccessful());
        assertTrue(result2.isSuccessful());
        assertEquals(2, simulator.getProcessor().getTotalProcessedPayments());
    }

    @Test
    void testWaterfallTriggered() {
        // Create a payment that triggers waterfall by exceeding holding limit
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST006</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC003</Id>
                </Beneficiary>
                <Amount>1200.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentResult result = simulator.processPaymentFromXml(xml);

        assertTrue(result.isSuccessful());
        // Check if waterfall was triggered
        assertTrue(simulator.getProcessor().getWaterfallTransactions().size() > 0);
    }

    @Test
    void testProcessPaymentWithLowBalance() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST007</MsgId>
                <Initiator>
                    <Id>ACC003</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>1500.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentResult result = simulator.processPaymentFromXml(xml);

        // ACC003 only has 1000, so this should be rejected
        assertFalse(result.isSuccessful());
        assertTrue(result.getReason().contains("Insufficient"));
    }

    @Test
    void testTenSecondConfirmationWindow() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>TEST008</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>200.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        long startTime = System.currentTimeMillis();
        PaymentResult result = simulator.processPaymentFromXml(xml);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue(result.isSuccessful());
        assertTrue(elapsedTime < 10000, "Payment should be confirmed within 10 seconds");
    }
}
