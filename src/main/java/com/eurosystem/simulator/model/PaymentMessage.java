package com.eurosystem.simulator.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentMessage {
    private String messageId;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String currency;
    private String purpose;

    public PaymentMessage(String messageId, String payerId, String payeeId,
                         BigDecimal amount, String currency, String purpose) {
        this.messageId = messageId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.currency = currency;
        this.purpose = purpose;
        this.createdAt = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public String getPayerId() {
        return payerId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPurpose() {
        return purpose;
    }

    @Override
    public String toString() {
        return "PaymentMessage{" +
                "messageId='" + messageId + '\'' +
                ", payerId='" + payerId + '\'' +
                ", payeeId='" + payeeId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
