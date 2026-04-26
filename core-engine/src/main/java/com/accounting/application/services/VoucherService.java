package com.accounting.application.services;

import com.accounting.domain.enums.Type;
import com.accounting.domain.enums.VoucherTypes;
import com.accounting.domain.models.Entry;
import com.accounting.domain.models.Voucher;
import com.accounting.domain.repository.VoucherRepo;

import java.time.LocalDate;
import java.util.List;

public class VoucherService {

    private final VoucherRepo voucherRepo;

    public VoucherService(VoucherRepo voucherRepo) {
        this.voucherRepo = voucherRepo;
    }

    public Voucher createVoucher(
            String companyId,
            VoucherTypes voucherType,
            LocalDate date,
            List<Entry> entries
    ) {

        if (entries == null || entries.size() < 2) {
            throw new RuntimeException("Voucher must have at least 2 entries");
        }

        double totalDr = 0;
        double totalCr = 0;

        for (Entry e : entries) {
            if (e.getType() == Type.DR) {
                totalDr += e.getAmount();
            } else {
                totalCr += e.getAmount();
            }
        }

        if (totalDr != totalCr) {
            throw new RuntimeException("Debit and Credit must be equal");
        }

        Voucher voucher = new Voucher(companyId, voucherType, date, entries);

        voucherRepo.save(voucher);

        return voucher;
    }


    public List<Voucher> getVouchersByCompany(String companyId) {
        return voucherRepo.findByCompanyId(companyId);
    }

    public List<Voucher> getVouchersByDateRange(
            String companyId,
            LocalDate from,
            LocalDate to
    ) {
        return voucherRepo.findByDateRange(companyId, from, to);
    }

    public Voucher getVoucher(String voucherId) {
        return voucherRepo.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
    }

    public List<Entry> getEntries(String voucherId) {
        return voucherRepo.findEntriesByVoucherId(voucherId);
    }
}