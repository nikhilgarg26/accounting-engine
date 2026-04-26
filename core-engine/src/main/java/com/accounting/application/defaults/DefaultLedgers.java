package com.accounting.application.defaults;

import com.accounting.domain.enums.Type;

import java.util.List;

public class DefaultLedgers {

    public static class LedgerDef {
        public final String name;
        public final String parentGroup;
        public final int openingBalance;
        public final Type type;

        public LedgerDef(String name, String parentGroup, int openingBalance, Type type) {
            this.name = name;
            this.parentGroup = parentGroup;
            this.openingBalance = openingBalance;
            this.type = type;
        }
    }

    public static List<DefaultLedgers.LedgerDef> get() {

        return List.of(
                new DefaultLedgers.LedgerDef("Cash", "Cash-in-hand", 0, Type.DR),
                new DefaultLedgers.LedgerDef("Profit / Loss A/c", "Capital Account", 0, Type.CR)
        );
    }
}
