package com.accounting.application.services;

import com.accounting.domain.enums.Nature;
import com.accounting.domain.enums.Type;
import com.accounting.domain.models.*;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.domain.repository.LedgerRepo;
import com.accounting.domain.repository.VoucherRepo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final VoucherRepo voucherRepo;

    private final LedgerRepo ledgerRepo;

    private final GroupRepo groupRepo;

    public ReportService(VoucherRepo voucherRepo, LedgerRepo ledgerRepo, GroupRepo groupRepo) {
        this.voucherRepo = voucherRepo;
        this.ledgerRepo = ledgerRepo;
        this.groupRepo = groupRepo;
    }

    // 🔥 Core method
    public Balance getLedgerBalance(
            String ledgerId,
            LocalDate from,
            LocalDate to
    ) {

        List<Entry> entries =
                voucherRepo.findEntriesByLedgerIdAndDate(ledgerId, from, to);

        double net = 0;

        for (Entry e : entries) {
            net += (e.getType() == Type.DR) ? e.getAmount() : -e.getAmount();
        }

        if (net > 0) return new Balance(net, Type.DR);
        if (net < 0) return new Balance(Math.abs(net), Type.CR);

        return new Balance(0, null);
    }

    // convenience method for CLI
    public String getLedgerBalanceFormatted(String ledgerId, LocalDate from,  LocalDate to) {
        return getLedgerBalance(ledgerId, from, to).toString();
    }

    public List<TrialBalanceRow> getTrialBalance(String companyId, LocalDate from, LocalDate to) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        List<TrialBalanceRow> rows = new ArrayList<>();

        for (Ledger l : ledgers) {

            Balance balance = getLedgerBalance(l.getId(), from, to);

            double dr = 0;
            double cr = 0;

            if (balance.getType() == Type.DR) {
                dr = balance.getAmount();
            } else if (balance.getType() == Type.CR) {
                cr = balance.getAmount();
            }

            rows.add(new TrialBalanceRow(l.getName(), dr, cr));
        }

        return rows;
    }

    public ProfitLossStatement getProfitLoss(String companyId, LocalDate from, LocalDate to) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        Map<String, Double> incomeMap = new LinkedHashMap<>();
        Map<String, Double> expenseMap = new LinkedHashMap<>();

        double totalIncome = 0;
        double totalExpense = 0;

        for (Ledger l : ledgers) {

            Balance balance = getLedgerBalance(l.getId(), from, to);

            if (balance.getType() == null) continue;

            Group group = groupRepo.findById(l.getParentGroup()).orElse(null);
            if (group == null) continue;

            double amount = balance.getAmount();

            if (group.getNature() == Nature.INCOME) {
                incomeMap.put(l.getName(), amount);
                totalIncome += amount;

            } else if (group.getNature() == Nature.EXPENSE) {
                expenseMap.put(l.getName(), amount);
                totalExpense += amount;
            }
        }

        double profit = totalIncome - totalExpense;

        return new ProfitLossStatement(
                incomeMap,
                expenseMap,
                totalIncome,
                totalExpense,
                profit
        );
    }

    public BalanceSheet getBalanceSheet(String companyId, LocalDate from, LocalDate to) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        Map<String, Double> assetMap = new LinkedHashMap<>();
        Map<String, Double> liabilityMap = new LinkedHashMap<>();

        double totalAssets = 0;
        double totalLiabilities = 0;

        for (Ledger l : ledgers) {

            Balance balance = getLedgerBalance(l.getId(), from, to);
            if (balance.getType() == null) continue;

            Group group = groupRepo.findById(l.getParentGroup()).orElse(null);
            if (group == null) continue;

            double amount = balance.getAmount();
            Type type = balance.getType();

            if (group.getNature() == Nature.ASSETS) {

                double signedAmount = (type == Type.DR) ? amount : -amount;

                assetMap.put(l.getName(), signedAmount);
                totalAssets += signedAmount;
            }

            // ---------------- LIABILITIES ----------------
            else if (group.getNature() == Nature.LIABILITY) {

                double signedAmount = (type == Type.CR) ? amount : -amount;

                liabilityMap.put(l.getName(), signedAmount);
                totalLiabilities += signedAmount;
            }
        }

        // 🔥 ADD PROFIT FROM P&L
        ProfitLossStatement pl = getProfitLoss(companyId, from, to);

        double profit = pl.getProfit();

        if (profit > 0) {
            liabilityMap.put("Profit", profit);
            totalLiabilities += profit;
        } else {
            assetMap.put("Loss", Math.abs(profit));
            totalAssets += Math.abs(profit);
        }

        return new BalanceSheet(
                assetMap,
                liabilityMap,
                totalAssets,
                totalLiabilities
        );
    }
}