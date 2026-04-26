package com.accounting.domain.models;

import java.util.UUID;

public class Company {

    private final String id;
    private String name;

    public Company(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public Company(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
