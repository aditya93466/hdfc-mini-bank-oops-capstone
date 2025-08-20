package com.hdfc.minibank.domain.accounts;

import java.math.BigDecimal;

public class SavingsAccount extends Account {

    private static final BigDecimal INTEREST_RATE = new BigDecimal("4.5");
    private static final BigDecimal MIN_BALANCE = new BigDecimal("1000");

    public SavingsAccount(String accountNo, String customerId, BigDecimal initialBalance) {
        super(accountNo, customerId, initialBalance);
    }

    @Override
    public BigDecimal getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return MIN_BALANCE;
    }

    @Override
    protected boolean canWithdraw(BigDecimal amount) {
        return getBalance().subtract(amount).compareTo(MIN_BALANCE) >= 0;
    }
}