package com.eurosystem.simulator.pacs;

import com.eurosystem.simulator.model.PaymentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pacs008Parser {
    private static final Logger logger = LoggerFactory.getLogger(Pacs008Parser.class);

    private static final Pattern PAYER_ID_PATTERN = Pattern.compile("<Initiator>.*?<Id>(.*?)</Id>.*?</Initiator>",
                                                                      Pattern.DOTALL);
    private static final Pattern PAYEE_ID_PATTERN = Pattern.compile("<Beneficiary>.*?<Id>(.*?)</Id>.*?</Beneficiary>",
                                                                      Pattern.DOTALL);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("<Amount>(.*?)</Amount>");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("<Ccy>(.*?)</Ccy>");
    private static final Pattern PURPOSE_PATTERN = Pattern.compile("<Purp>.*?<Cd>(.*?)</Cd>.*?</Purp>",
                                                                    Pattern.DOTALL);
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("<MsgId>(.*?)</MsgId>");

    public static PaymentMessage parseFromXml(String xmlContent) throws Pacs008ParseException {
        try {
            String messageId = extractValue(xmlContent, MESSAGE_ID_PATTERN, "UNKNOWN_MSG_ID");
            String payerId = extractValue(xmlContent, PAYER_ID_PATTERN, "UNKNOWN_PAYER");
            String payeeId = extractValue(xmlContent, PAYEE_ID_PATTERN, "UNKNOWN_PAYEE");
            String amountStr = extractValue(xmlContent, AMOUNT_PATTERN, "0");
            String currency = extractValue(xmlContent, CURRENCY_PATTERN, "EUR");
            String purpose = extractValue(xmlContent, PURPOSE_PATTERN, "OTHR");

            BigDecimal amount = new BigDecimal(amountStr);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new Pacs008ParseException("Invalid amount: " + amountStr);
            }

            if (!currency.equalsIgnoreCase("EUR")) {
                throw new Pacs008ParseException("Only EUR currency is supported, got: " + currency);
            }

            logger.info("Parsed PACS.008 message: {}, Payer: {}, Payee: {}, Amount: {} {}",
                       messageId, payerId, payeeId, amount, currency);

            return new PaymentMessage(messageId, payerId, payeeId, amount, currency, purpose);
        } catch (NumberFormatException e) {
            throw new Pacs008ParseException("Invalid amount format", e);
        }
    }

    public static PaymentMessage parseFromFile(Path filePath) throws Pacs008ParseException, IOException {
        String xmlContent = Files.readString(filePath);
        return parseFromXml(xmlContent);
    }

    private static String extractValue(String xml, Pattern pattern, String defaultValue) {
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return defaultValue;
    }

    public static class Pacs008ParseException extends Exception {
        public Pacs008ParseException(String message) {
            super(message);
        }

        public Pacs008ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
