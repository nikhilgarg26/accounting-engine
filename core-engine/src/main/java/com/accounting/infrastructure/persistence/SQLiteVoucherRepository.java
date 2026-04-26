package com.accounting.infrastructure.persistence;

import com.accounting.domain.enums.Type;
import com.accounting.domain.enums.VoucherTypes;
import com.accounting.domain.models.Entry;
import com.accounting.domain.models.Voucher;
import com.accounting.domain.repository.VoucherRepo;
import com.accounting.infrastructure.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteVoucherRepository implements VoucherRepo {

    @Override
    public void save(Voucher voucher) {

        String voucherSql = """
            INSERT INTO vouchers (id, company_id, voucher_type, date)
            VALUES (?, ?, ?, ?)
        """;

        String entrySql = """
            INSERT INTO entries (id, voucher_id, ledger_id, amount, type)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection()) {

            conn.setAutoCommit(false); // 🔥 IMPORTANT

            try (PreparedStatement voucherStmt = conn.prepareStatement(voucherSql);
                 PreparedStatement entryStmt = conn.prepareStatement(entrySql)) {

                // 1. Insert voucher
                voucherStmt.setString(1, voucher.getId());
                voucherStmt.setString(2, voucher.getCompanyId());
                voucherStmt.setString(3, String.valueOf(voucher.getVoucherType()));
                voucherStmt.setString(4, voucher.getDate().toString());

                voucherStmt.executeUpdate();

                // 2. Insert entries
                for (Entry e : voucher.getEntries()) {

                    entryStmt.setString(1, e.getId());
                    entryStmt.setString(2, voucher.getId());
                    entryStmt.setString(3, e.getLedgerId());
                    entryStmt.setDouble(4, e.getAmount());
                    entryStmt.setString(5, e.getType().name());

                    entryStmt.addBatch();
                }

                entryStmt.executeBatch();

                conn.commit(); // ✅ success

            } catch (Exception e) {
                conn.rollback(); // ❌ rollback on failure
                throw new RuntimeException("Failed to save voucher", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
    }

    @Override
    public Optional<Voucher> findById(String id) {

        String sql = "SELECT * FROM vouchers WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapVoucher(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch voucher", e);
        }
    }

    @Override
    public List<Voucher> findByCompanyId(String companyId) {

        String sql = "SELECT * FROM vouchers WHERE company_id = ?";
        List<Voucher> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapVoucher(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch vouchers", e);
        }
    }

    @Override
    public List<Voucher> findByDateRange(String companyId, LocalDate from, LocalDate to) {

        String sql = """
            SELECT * FROM vouchers
            WHERE company_id = ?
            AND date BETWEEN ? AND ?
        """;

        List<Voucher> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyId);
            stmt.setString(2, from.toString());
            stmt.setString(3, to.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapVoucher(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch vouchers by date", e);
        }
    }

    @Override
    public List<Entry> findEntriesByVoucherId(String voucherId) {

        String sql = "SELECT * FROM entries WHERE voucher_id = ?";
        List<Entry> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, voucherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapEntry(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch entries", e);
        }
    }

    // ----------------- MAPPERS -----------------

    private Voucher mapVoucher(ResultSet rs) throws SQLException {

        return new Voucher(
                rs.getString("id"),
                rs.getString("company_id"),
                VoucherTypes.valueOf(rs.getString("voucher_type")),
                LocalDate.parse(rs.getString("date")),
                List.of()
        );
    }

    private Entry mapEntry(ResultSet rs) throws SQLException {

        return new Entry(
                rs.getString("id"),
                rs.getString("ledger_id"),
                rs.getDouble("amount"),
                Type.valueOf(rs.getString("type"))
        );
    }
}