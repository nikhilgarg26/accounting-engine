package com.accounting.domain.models;

import java.util.Map;

public class BalanceSheet {

    private final Map<String, Double> assets;
    private final Map<String, Double> liabilities;
    private final double totalAssets;
    private final double totalLiabilities;

    public BalanceSheet(Map<String, Double> assets,
                        Map<String, Double> liabilities,
                        double totalAssets,
                        double totalLiabilities) {
        this.assets = assets;
        this.liabilities = liabilities;
        this.totalAssets = totalAssets;
        this.totalLiabilities = totalLiabilities;
    }

    public Map<String, Double> getAssets() { return assets; }
    public Map<String, Double> getLiabilities() { return liabilities; }
    public double getTotalAssets() { return totalAssets; }
    public double getTotalLiabilities() { return totalLiabilities; }
}