package com.accounting.infrastructure.db;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // COMPANY TABLE
            String createCompanyTable = """
                CREATE TABLE IF NOT EXISTS companies (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL
                );
            """;

            String createGroupTable = """
                CREATE TABLE IF NOT EXISTS groups (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    nature TEXT NOT NULL,
                    parent_group_id TEXT,
                    company_id TEXT NOT NULL,
                    FOREIGN KEY (parent_group_id) REFERENCES groups(id),
                    FOREIGN KEY (company_id) REFERENCES companies(id)
                );
            """;

            String createLedgerTable = """
                CREATE TABLE IF NOT EXISTS ledgers (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    parent_group_id TEXT,
                    company_id TEXT NOT NULL,
                    opening_balance REAL,
                    type TEXT,
                    FOREIGN KEY (parent_group_id) REFERENCES groups(id),
                    FOREIGN KEY (company_id) REFERENCES companies(id)
                );
            """;

            String createVoucherTable = """
                CREATE TABLE IF NOT EXISTS vouchers (
                                              id TEXT PRIMARY KEY,
                                              company_id TEXT NOT NULL,
                                              voucher_type TEXT,
                                              date TEXT
            """;

            String createEntryTable = """
                CREATE TABLE IF NOT EXISTS entries (
                                            id TEXT PRIMARY KEY,
                                            voucher_id TEXT NOT NULL,
                                            ledger_id TEXT NOT NULL,
                                            amount REAL,
                                            type TEXT,
                                            FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
                                            FOREIGN KEY (ledger_id) REFERENCES ledgers(id)
                        );
            """;



            stmt.execute(createLedgerTable);
            stmt.execute(createCompanyTable);
            stmt.execute(createGroupTable);
            stmt.execute(createVoucherTable);
            stmt.execute(createEntryTable);

        } catch (Exception e) {
            throw new RuntimeException("DB initialization failed", e);
        }
    }
}