package com.eurosystem.simulator.model;

import java.time.LocalDateTime;

public class PaymentResult {
    private String messageId;
    private boolean successful;
    private String status;
    private String reason;
    private LocalDateTime processedAt;

    public PaymentResult(String messageId, boolean successful, String status, String reason) {
        this.messageId = messageId;
        this.successful = successful;
        this.status = status;
        this.reason = reason;
        this.processedAt = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "messageId='" + messageId + '\'' +
                ", successful=" + successful +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
