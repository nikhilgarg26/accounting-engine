package com.accounting.application.defaults;

import com.accounting.domain.enums.Nature;

import java.util.List;

public class DefaultGroups {

    public static class GroupDef {
        public final String name;
        public final Nature nature;
        public final String parentName;

        public GroupDef(String name, Nature nature, String parentName) {
            this.name = name;
            this.nature = nature;
            this.parentName = parentName;
        }
    }

    public static List<GroupDef> get() {

        return List.of(
                new GroupDef("Branch / Divisions", Nature.LIABILITY, null),
                new GroupDef("Capital Account", Nature.LIABILITY, null),
                new GroupDef("Current Assets", Nature.ASSETS, null),
                new GroupDef("Current Liabilities", Nature.LIABILITY, null),
                new GroupDef("Direct Expenses", Nature.EXPENSE, null),
                new GroupDef("Direct Incomes", Nature.INCOME, null),
                new GroupDef("Fixed Assets", Nature.ASSETS, null),
                new GroupDef("Indirect Expenses", Nature.EXPENSE, null),
                new GroupDef("Indirect Incomes", Nature.INCOME, null),
                new GroupDef("Investments", Nature.ASSETS, null),
                new GroupDef("Loans", Nature.LIABILITY, null),
                new GroupDef("Misc. Expenses", Nature.ASSETS, null),
                new GroupDef("Purchase Accounts", Nature.EXPENSE, null),
                new GroupDef("Sales Accounts", Nature.INCOME, null),
                new GroupDef("Suspense A/c", Nature.LIABILITY, null),

                new GroupDef("Bank Accounts", Nature.ASSETS, "Current Assets"),
                new GroupDef("Bank OD A/c", Nature.LIABILITY, "Loans"),
                new GroupDef("Cash-in-hand", Nature.ASSETS, "Current Assets"),
                new GroupDef("Deposits", Nature.ASSETS, "Current Assets"),
                new GroupDef("Duties & Taxes", Nature.LIABILITY, "Current Liabilities"),
                new GroupDef("Loans & Advances", Nature.ASSETS, "Current Assets"),
                new GroupDef("Provisions", Nature.LIABILITY, "Current Liabilities"),
                new GroupDef("Reserves & Surplus", Nature.LIABILITY, "Capital Account"),
                new GroupDef("Secured Loans", Nature.LIABILITY, "Loans"),
                new GroupDef("Stock-in-hand", Nature.ASSETS, "Current Assets"),
                new GroupDef("Sundry Creditors", Nature.LIABILITY, "Current Liabilities"),
                new GroupDef("Sundry Debtors", Nature.ASSETS, "Current Assets"),
                new GroupDef("Unsecured Loans", Nature.LIABILITY, "Loans")
        );
    }
}