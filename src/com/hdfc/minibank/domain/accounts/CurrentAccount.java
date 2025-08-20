package com.hdfc.minibank.domain.accounts;

import java.math.BigDecimal;

public class CurrentAccount extends Account {

    private static final BigDecimal INTEREST_RATE = BigDecimal.ZERO;
    private static final BigDecimal MIN_BALANCE = BigDecimal.ZERO;

    public CurrentAccount(String accountNo, String customerId, BigDecimal initialBalance) {
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
        return getBalance().compareTo(amount) >= 0;
    }
}