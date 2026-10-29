package com.eurosystem.simulator.pacs;

import com.eurosystem.simulator.model.PaymentMessage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class Pacs008ParserTest {

    @Test
    void testParseValidPacs008Message() throws Pacs008Parser.Pacs008ParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>MSG001</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>1500.50</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        PaymentMessage message = Pacs008Parser.parseFromXml(xml);

        assertNotNull(message);
        assertEquals("MSG001", message.getMessageId());
        assertEquals("ACC001", message.getPayerId());
        assertEquals("ACC002", message.getPayeeId());
        assertEquals(new BigDecimal("1500.50"), message.getAmount());
        assertEquals("EUR", message.getCurrency());
    }

    @Test
    void testParseInvalidCurrency() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>MSG002</MsgId>
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

        assertThrows(Pacs008Parser.Pacs008ParseException.class,
                    () -> Pacs008Parser.parseFromXml(xml));
    }

    @Test
    void testParseInvalidAmount() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>MSG003</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>-500.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        assertThrows(Pacs008Parser.Pacs008ParseException.class,
                    () -> Pacs008Parser.parseFromXml(xml));
    }

    @Test
    void testParseZeroAmount() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>MSG004</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>0.00</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>OTHR</Cd>
                </Purp>
            </Document>
            """;

        assertThrows(Pacs008Parser.Pacs008ParseException.class,
                    () -> Pacs008Parser.parseFromXml(xml));
    }

    @Test
    void testParseWithMissingOptionalFields() throws Pacs008Parser.Pacs008ParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <MsgId>MSG005</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>750.00</Amount>
                <Ccy>EUR</Ccy>
            </Document>
            """;

        PaymentMessage message = Pacs008Parser.parseFromXml(xml);

        assertNotNull(message);
        assertEquals("MSG005", message.getMessageId());
        assertEquals(new BigDecimal("750.00"), message.getAmount());
        assertEquals("OTHR", message.getPurpose()); // default value
    }

    @Test
    void testParseComplexPacs008Message() throws Pacs008Parser.Pacs008ParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.003.02">
                <MsgId>20261029001</MsgId>
                <Initiator>
                    <Id>ACC001</Id>
                </Initiator>
                <Beneficiary>
                    <Id>ACC002</Id>
                </Beneficiary>
                <Amount>2500.75</Amount>
                <Ccy>EUR</Ccy>
                <Purp>
                    <Cd>SALA</Cd>
                </Purp>
            </Document>
            """;

        PaymentMessage message = Pacs008Parser.parseFromXml(xml);

        assertNotNull(message);
        assertEquals("EUR", message.getCurrency());
        assertEquals(new BigDecimal("2500.75"), message.getAmount());
    }
}
