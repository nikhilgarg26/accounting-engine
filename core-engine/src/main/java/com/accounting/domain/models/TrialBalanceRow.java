package com.accounting.domain.models;

public class TrialBalanceRow {

    private final String ledgerName;
    private final double debit;
    private final double credit;

    public TrialBalanceRow(String ledgerName, double debit, double credit) {
        this.ledgerName = ledgerName;
        this.debit = debit;
        this.credit = credit;
    }

    public String getLedgerName() {
        return ledgerName;
    }

    public double getDebit() {
        return debit;
    }

    public double getCredit() {
        return credit;
    }
}