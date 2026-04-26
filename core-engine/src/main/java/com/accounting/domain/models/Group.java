package com.accounting.domain.models;

import com.accounting.domain.enums.Nature;

import java.util.UUID;

public class Group {

    private final String id;
    private String name;
    private Nature nature;
    private String parentGroupId;
    private String companyId;

    public Group(String name, Nature nature, String parentGroupId, String companyId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.nature = nature;
        this.parentGroupId = parentGroupId;
        this.companyId = companyId;
    }

    public Group(String id, String name, Nature nature, String parentGroupId, String companyId) {
        this.id = id;
        this.name = name;
        this.nature = nature;
        this.parentGroupId = parentGroupId;
        this.companyId = companyId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Nature getNature() {
        return nature;
    }

    public String getParentGroupId() {
        return parentGroupId;
    }

    public String getCompanyId() {
        return companyId;
    }
}