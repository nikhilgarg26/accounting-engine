package com.accounting.domain.models;

import com.accounting.domain.enums.VoucherTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Voucher {

    private final String id;
    private final String companyId;
    private final VoucherTypes voucherType;
    private final LocalDate date;
    private final List<Entry> entries;

    public Voucher(String companyId, VoucherTypes voucherType, LocalDate date, List<Entry> entries) {
        this.id = UUID.randomUUID().toString();
        this.companyId = companyId;
        this.voucherType = voucherType;
        this.date = date;
        this.entries = entries;
    }

    public Voucher(String id, String companyId, VoucherTypes voucherType, LocalDate date, List<Entry> entries) {
        this.id = id;
        this.companyId = companyId;
        this.voucherType = voucherType;
        this.date = date;
        this.entries = entries;
    }


    public List<Entry> getEntries() {
        return entries;
    }

    public String getCompanyId() {
        return companyId;
    }

    public VoucherTypes getVoucherType() {
        return voucherType;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getId() {
        return id;
    }
}