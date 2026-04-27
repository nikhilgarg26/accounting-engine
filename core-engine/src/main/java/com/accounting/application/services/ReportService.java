package com.accounting.application.services;

import com.accounting.domain.enums.Type;
import com.accounting.domain.models.Balance;
import com.accounting.domain.models.Entry;
import com.accounting.domain.repository.VoucherRepo;

import java.util.List;

public class ReportService {

    private final VoucherRepo voucherRepo;

    public ReportService(VoucherRepo voucherRepo) {
        this.voucherRepo = voucherRepo;
    }

    // 🔥 Core method
    public Balance getLedgerBalance(String ledgerId) {

        List<Entry> entries = voucherRepo.findEntriesByLedgerId(ledgerId);

        double net = 0;

        for (Entry e : entries) {
            if (e.getType() == Type.DR) {
                net += e.getAmount();
            } else {
                net -= e.getAmount();
            }
        }

        // convert net → Balance object
        if (net > 0) {
            return new Balance(net, Type.DR);
        } else if (net < 0) {
            return new Balance(Math.abs(net), Type.CR);
        } else {
            return new Balance(0, null); // zero balance
        }
    }

    // convenience method for CLI
    public String getLedgerBalanceFormatted(String ledgerId) {
        return getLedgerBalance(ledgerId).toString();
    }
}