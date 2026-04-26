package com.accounting.domain.models;

import com.accounting.domain.enums.Type;

import java.util.UUID;

public class Entry {

    private final String id;
    private final String ledgerId;
    private final double amount;
    private final Type type; // DR / CR

    public Entry(String ledgerId, double amount, Type type) {
        this.id = UUID.randomUUID().toString();
        this.ledgerId = ledgerId;
        this.amount = amount;
        this.type = type;
    }

    public Entry(String id, String ledgerId, double amount, Type type) {
        this.id = id;
        this.ledgerId = ledgerId;
        this.amount = amount;
        this.type = type;
    }

    public String getLedgerId() {
        return ledgerId;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }
}