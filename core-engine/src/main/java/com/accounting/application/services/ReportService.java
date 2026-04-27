package com.accounting.application.services;

import com.accounting.domain.enums.Nature;
import com.accounting.domain.enums.Type;
import com.accounting.domain.models.*;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.domain.repository.LedgerRepo;
import com.accounting.domain.repository.VoucherRepo;

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

    public List<TrialBalanceRow> getTrialBalance(String companyId) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        List<TrialBalanceRow> rows = new ArrayList<>();

        for (Ledger l : ledgers) {

            Balance balance = getLedgerBalance(l.getId());

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

    public ProfitLossStatement getProfitLoss(String companyId) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        Map<String, Double> incomeMap = new LinkedHashMap<>();
        Map<String, Double> expenseMap = new LinkedHashMap<>();

        double totalIncome = 0;
        double totalExpense = 0;

        for (Ledger l : ledgers) {

            Balance balance = getLedgerBalance(l.getId());

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

    public BalanceSheet getBalanceSheet(String companyId) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        Map<String, Double> assetMap = new LinkedHashMap<>();
        Map<String, Double> liabilityMap = new LinkedHashMap<>();

        double totalAssets = 0;
        double totalLiabilities = 0;

        for (Ledger l : ledgers) {

            Balance balance = getLedgerBalance(l.getId());
            if (balance.getType() == null) continue;

            Group group = groupRepo.findById(l.getParentGroup()).orElse(null);
            if (group == null) continue;

            double amount = balance.getAmount();

            if (group.getNature() == Nature.ASSETS) {

                assetMap.put(l.getName(), amount);
                totalAssets += amount;

            } else if (group.getNature() == Nature.LIABILITY) {

                liabilityMap.put(l.getName(), amount);
                totalLiabilities += amount;
            }
        }

        // 🔥 ADD PROFIT FROM P&L
        ProfitLossStatement pl = getProfitLoss(companyId);

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