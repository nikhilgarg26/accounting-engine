package com.accounting.domain.repository;

import com.accounting.domain.models.Entry;
import com.accounting.domain.models.Voucher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VoucherRepo {

    void save(Voucher voucher);

    Optional<Voucher> findById(String id);

    List<Voucher> findByCompanyId(String companyId);

    List<Voucher> findByDateRange(String companyId, LocalDate from, LocalDate to);

    List<Entry> findEntriesByVoucherId(String voucherId);

    List<Entry> findEntriesByLedgerId(String ledgerId);

    List<Entry> findEntriesByLedgerIdAndDate(
            String ledgerId,
            LocalDate from,
            LocalDate to
    );
}