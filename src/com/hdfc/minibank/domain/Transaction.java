package com.hdfc.minibank.domain;

import com.hdfc.minibank.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private final String id;
    private final String accountNo;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final String notes;

    public Transaction(String id, String accountNo, TransactionType type, BigDecimal amount, LocalDateTime timestamp, String notes) {
        this.id = id;
        this.accountNo = accountNo;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public String getId() { return id; }
    public String getAccountNo() { return accountNo; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getNotes() { return notes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}