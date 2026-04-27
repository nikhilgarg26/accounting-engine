package com.accounting.domain.models;

import java.util.Map;

public class ProfitLossStatement {

    private final Map<String, Double> income;
    private final Map<String, Double> expense;
    private final double totalIncome;
    private final double totalExpense;
    private final double profit;

    public ProfitLossStatement(Map<String, Double> income,
                               Map<String, Double> expense,
                               double totalIncome,
                               double totalExpense,
                               double profit) {
        this.income = income;
        this.expense = expense;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.profit = profit;
    }

    public Map<String, Double> getIncome() { return income; }
    public Map<String, Double> getExpense() { return expense; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public double getProfit() { return profit; }
}