package com.accounting.domain.models;

import com.accounting.domain.enums.Type;

public class Balance {

    private final double amount; // always non-negative
    private final Type type;     // DR or CR (null if zero)

    public Balance(double amount, Type type) {
        this.amount = amount;
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }

    public boolean isZero() {
        return amount == 0;
    }

    @Override
    public String toString() {
        if (isZero()) return "0.00";
        return String.format("%.2f %s", amount, type.name());
    }
}