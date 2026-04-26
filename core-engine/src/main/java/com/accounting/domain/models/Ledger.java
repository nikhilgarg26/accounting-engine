package com.accounting.domain.models;

import com.accounting.domain.enums.Type;

import java.util.UUID;

public class Ledger {

    private final String id;
    private String name;
    private String parentGroup;
    private double openingBalance;
    private Type type;
    private String companyId;

    public Ledger(String name, String parentGroup, double openingBalance, Type type, String companyId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.parentGroup = parentGroup;
        this.openingBalance = openingBalance;
        this.type = type;
        this.companyId = companyId;
    }

    public Ledger(String id, String name, String parentGroup, double openingBalance, Type type, String companyId) {
        this.id = id;
        this.name = name;
        this.parentGroup = parentGroup;
        this.openingBalance = openingBalance;
        this.type = type;
        this.companyId = companyId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParentGroup() {
        return parentGroup;
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public Type getType() {
        return type;
    }

    public String getCompanyId() {
        return companyId;
    }
}
