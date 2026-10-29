package com.eurosystem.simulator.model;

import java.math.BigDecimal;

public class DigitalEuroAccount {
    private String accountId;
    private String personId;
    private BigDecimal balance;
    private BigDecimal holdingLimit;
    private String linkedBankAccount;

    public DigitalEuroAccount(String accountId, String personId, BigDecimal initialBalance,
                            BigDecimal holdingLimit, String linkedBankAccount) {
        this.accountId = accountId;
        this.personId = personId;
        this.balance = initialBalance;
        this.holdingLimit = holdingLimit;
        this.linkedBankAccount = linkedBankAccount;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPersonId() {
        return personId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getHoldingLimit() {
        return holdingLimit;
    }

    public void setHoldingLimit(BigDecimal holdingLimit) {
        this.holdingLimit = holdingLimit;
    }

    public String getLinkedBankAccount() {
        return linkedBankAccount;
    }

    public void setLinkedBankAccount(String linkedBankAccount) {
        this.linkedBankAccount = linkedBankAccount;
    }

    public boolean canHold(BigDecimal amount) {
        return balance.add(amount).compareTo(holdingLimit) <= 0;
    }

    public BigDecimal getExcessAmount() {
        if (balance.compareTo(holdingLimit) > 0) {
            return balance.subtract(holdingLimit);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "DigitalEuroAccount{" +
                "accountId='" + accountId + '\'' +
                ", personId='" + personId + '\'' +
                ", balance=" + balance +
                ", holdingLimit=" + holdingLimit +
                ", linkedBankAccount='" + linkedBankAccount + '\'' +
                '}';
    }
}
