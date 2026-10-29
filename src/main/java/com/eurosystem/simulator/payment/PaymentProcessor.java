package com.eurosystem.simulator.payment;

import com.eurosystem.simulator.model.PaymentMessage;
import com.eurosystem.simulator.model.PaymentResult;
import com.eurosystem.simulator.model.DigitalEuroAccount;
import com.eurosystem.simulator.model.WaterfallTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessor.class);
    private static final long INSTANT_PAYMENT_TIMEOUT_MS = 10000; // 10 seconds for TIPS
    private static final String CONFIRMATION_STATUS = "CONFIRMED";
    private static final String REJECTED_STATUS = "REJECTED";

    private final Map<String, DigitalEuroAccount> accounts;
    private final Map<String, PaymentResult> processedPayments;
    private final List<WaterfallTransaction> waterfallTransactions;

    public PaymentProcessor() {
        this.accounts = new ConcurrentHashMap<>();
        this.processedPayments = new ConcurrentHashMap<>();
        this.waterfallTransactions = Collections.synchronizedList(new ArrayList<>());
    }

    public void registerAccount(DigitalEuroAccount account) {
        accounts.put(account.getAccountId(), account);
        logger.info("Registered account: {}", account);
    }

    public PaymentResult processPayment(PaymentMessage message) {
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Processing payment message: {}", message.getMessageId());

            // Validate payment message
            if (!isValidPaymentMessage(message)) {
                return rejectPayment(message.getMessageId(), "Invalid payment message");
            }

            // Retrieve accounts
            DigitalEuroAccount payerAccount = accounts.get(message.getPayerId());
            DigitalEuroAccount payeeAccount = accounts.get(message.getPayeeId());

            if (payerAccount == null || payeeAccount == null) {
                return rejectPayment(message.getMessageId(), "Invalid payer or payee account");
            }

            // Check payer has sufficient balance
            if (payerAccount.getBalance().compareTo(message.getAmount()) < 0) {
                return rejectPayment(message.getMessageId(), "Insufficient funds");
            }

            // Execute the payment within 10-second window
            synchronized (payerAccount) {
                synchronized (payeeAccount) {
                    // Double-check before executing
                    if (payerAccount.getBalance().compareTo(message.getAmount()) < 0) {
                        return rejectPayment(message.getMessageId(), "Insufficient funds");
                    }

                    // Transfer from payer to payee
                    payerAccount.setBalance(payerAccount.getBalance().subtract(message.getAmount()));
                    payeeAccount.setBalance(payeeAccount.getBalance().add(message.getAmount()));

                    logger.info("Payment transferred: {} EUR from {} to {}",
                               message.getAmount(), message.getPayerId(), message.getPayeeId());
                }
            }

            // Check if payee's balance exceeds holding limit (waterfall mechanism)
            BigDecimal excessAmount = payeeAccount.getExcessAmount();
            if (excessAmount.compareTo(BigDecimal.ZERO) > 0) {
                executeWaterfall(payeeAccount, excessAmount);
            }

            // Verify confirmation within 10-second limit
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > INSTANT_PAYMENT_TIMEOUT_MS) {
                logger.warn("Payment confirmation exceeded 10-second limit: {} ms", elapsedTime);
                return rejectPayment(message.getMessageId(), "Confirmation timeout exceeded");
            }

            PaymentResult result = new PaymentResult(message.getMessageId(), true,
                                                     CONFIRMATION_STATUS,
                                                     "Payment confirmed within 10 seconds");
            processedPayments.put(message.getMessageId(), result);
            logger.info("Payment result: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("Error processing payment: {}", message.getMessageId(), e);
            return rejectPayment(message.getMessageId(), "Processing error: " + e.getMessage());
        }
    }

    private PaymentResult rejectPayment(String messageId, String reason) {
        PaymentResult result = new PaymentResult(messageId, false, REJECTED_STATUS, reason);
        processedPayments.put(messageId, result);
        logger.info("Payment rejected: {}", result);
        return result;
    }

    private void executeWaterfall(DigitalEuroAccount account, BigDecimal excessAmount) {
        String linkedBankAccount = account.getLinkedBankAccount();
        if (linkedBankAccount == null || linkedBankAccount.isEmpty()) {
            logger.warn("No linked bank account for waterfall transfer from: {}", account.getAccountId());
            return;
        }

        String transactionId = UUID.randomUUID().toString();
        WaterfallTransaction transaction = new WaterfallTransaction(
            transactionId,
            account.getAccountId(),
            linkedBankAccount,
            excessAmount,
            "Automatic waterfall transfer due to holding limit exceeded"
        );

        account.setBalance(account.getBalance().subtract(excessAmount));
        waterfallTransactions.add(transaction);

        logger.info("Waterfall transfer executed: {} EUR from {} to {}",
                   excessAmount, account.getAccountId(), linkedBankAccount);
    }

    private boolean isValidPaymentMessage(PaymentMessage message) {
        return message != null &&
               message.getMessageId() != null && !message.getMessageId().isEmpty() &&
               message.getPayerId() != null && !message.getPayerId().isEmpty() &&
               message.getPayeeId() != null && !message.getPayeeId().isEmpty() &&
               message.getAmount() != null && message.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
               "EUR".equalsIgnoreCase(message.getCurrency());
    }

    public PaymentResult getPaymentResult(String messageId) {
        return processedPayments.get(messageId);
    }

    public DigitalEuroAccount getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public List<WaterfallTransaction> getWaterfallTransactions() {
        return new ArrayList<>(waterfallTransactions);
    }

    public int getTotalProcessedPayments() {
        return processedPayments.size();
    }

    public void reset() {
        accounts.clear();
        processedPayments.clear();
        waterfallTransactions.clear();
        logger.info("Payment processor reset");
    }
}
