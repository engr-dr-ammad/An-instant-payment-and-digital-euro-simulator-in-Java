package com.eurosystem.simulator.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WaterfallTransaction {
    private String transactionId;
    private String fromAccount;
    private String toLinkedBankAccount;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String reason;

    public WaterfallTransaction(String transactionId, String fromAccount, String toLinkedBankAccount,
                              BigDecimal amount, String reason) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toLinkedBankAccount = toLinkedBankAccount;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToLinkedBankAccount() {
        return toLinkedBankAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "WaterfallTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", fromAccount='" + fromAccount + '\'' +
                ", toLinkedBankAccount='" + toLinkedBankAccount + '\'' +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                ", reason='" + reason + '\'' +
                '}';
    }
}
