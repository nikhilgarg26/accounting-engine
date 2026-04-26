package com.accounting.infrastructure.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:accounting.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }
}