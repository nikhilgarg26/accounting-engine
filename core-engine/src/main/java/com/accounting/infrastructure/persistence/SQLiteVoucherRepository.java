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
                Voucher v = mapVoucher(rs);

                // 🔥 attach entries
                List<Entry> entries = findEntriesByVoucherId(id);

                return Optional.of(new Voucher(
                        v.getId(),
                        v.getCompanyId(),
                        v.getVoucherType(),
                        v.getDate(),
                        entries
                ));
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
                Voucher v = mapVoucher(rs);

                List<Entry> entries = findEntriesByVoucherId(v.getId());

                list.add(new Voucher(
                        v.getId(),
                        v.getCompanyId(),
                        v.getVoucherType(),
                        v.getDate(),
                        entries
                ));
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
                Voucher v = mapVoucher(rs);

                List<Entry> entries = findEntriesByVoucherId(v.getId());

                list.add(new Voucher(
                        v.getId(),
                        v.getCompanyId(),
                        v.getVoucherType(),
                        v.getDate(),
                        entries
                ));
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

    @Override
    public List<Entry> findEntriesByLedgerId(String ledgerId) {
        String sql = "SELECT * FROM entries WHERE ledger_id = ?";
        List<Entry> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ledgerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapEntry(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch entries", e);
        }
    }

    @Override
    public List<Entry> findEntriesByLedgerIdAndDate(
            String ledgerId,
            LocalDate from,
            LocalDate to
    ) {

        StringBuilder sql = new StringBuilder("""
        SELECT e.*
        FROM entries e
        JOIN vouchers v ON e.voucher_id = v.id
        WHERE e.ledger_id = ?
    """);

        // 🔥 Dynamic conditions
        if (from != null && to != null) {
            sql.append(" AND v.date BETWEEN ? AND ?");
        } else if (to != null) {
            sql.append(" AND v.date <= ?");
        } else if (from != null) {
            sql.append(" AND v.date >= ?");
        }

        sql.append(" ORDER BY v.date ASC");

        List<Entry> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setString(index++, ledgerId);

            // 🔥 Set parameters carefully
            if (from != null && to != null) {
                stmt.setString(index++, from.toString());
                stmt.setString(index++, to.toString());
            } else if (to != null) {
                stmt.setString(index++, to.toString());
            } else if (from != null) {
                stmt.setString(index++, from.toString());
            }

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