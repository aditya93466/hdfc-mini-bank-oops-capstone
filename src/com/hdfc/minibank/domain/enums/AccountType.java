package com.hdfc.minibank.domain.enums;

public enum AccountType {
    SAVINGS("Savings"),
    CURRENT("Current");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}