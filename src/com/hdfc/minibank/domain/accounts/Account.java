package com.hdfc.minibank.domain.accounts;

import com.hdfc.minibank.exceptions.InsufficientBalanceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Account {
    private final String accountNo;
    private final String customerId;
    protected BigDecimal balance;
    private final LocalDateTime createdAt;

    public Account(String accountNo, String customerId, BigDecimal initialBalance) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = initialBalance == null ? BigDecimal.ZERO : initialBalance;
        this.createdAt = LocalDateTime.now();
    }

    public String getAccountNo() { return accountNo; }
    public String getCustomerId() { return customerId; }
    public synchronized BigDecimal getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public abstract BigDecimal getInterestRate();
    public abstract BigDecimal getMinimumBalance();

    public BigDecimal calculateInterest() {
        return balance.multiply(getInterestRate()).divide(new BigDecimal("100"));
    }

    protected abstract boolean canWithdraw(BigDecimal amount);

    public synchronized void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public synchronized void withdraw(BigDecimal amount) throws InsufficientBalanceException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (!canWithdraw(amount)) {
            throw new InsufficientBalanceException("Insufficient balance or minimum balance constraint violated");
        }
        this.balance = this.balance.subtract(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(accountNo, account.accountNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNo);
    }
}