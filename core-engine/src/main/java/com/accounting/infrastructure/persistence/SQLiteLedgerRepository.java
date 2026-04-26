package com.accounting.infrastructure.persistence;

import com.accounting.domain.enums.Type;
import com.accounting.domain.models.Ledger;
import com.accounting.domain.repository.LedgerRepo;
import com.accounting.infrastructure.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteLedgerRepository implements LedgerRepo {

    @Override
    public void save(Ledger ledger) {
        String sql = """
            INSERT INTO ledgers (id, name, parent_group_id, company_id, opening_balance, type)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ledger.getId());
            stmt.setString(2, ledger.getName());
            stmt.setString(3, ledger.getParentGroup()); // or getGroupId()
            stmt.setString(4, ledger.getCompanyId());
            stmt.setDouble(5, ledger.getOpeningBalance());
            stmt.setString(6, ledger.getType().name());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ledger", e);
        }
    }

    @Override
    public Optional<Ledger> findById(String id) {

        String sql = "SELECT * FROM ledgers WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ledger", e);
        }
    }

    @Override
    public Optional<Ledger> findByNameAndCompany(String name, String companyId) {

        String sql = """
            SELECT * FROM ledgers
            WHERE name = ? AND company_id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, companyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ledger by name", e);
        }
    }

    @Override
    public List<Ledger> findByCompanyId(String companyId) {

        String sql = "SELECT * FROM ledgers WHERE company_id = ?";
        List<Ledger> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ledgers by company", e);
        }
    }

    @Override
    public List<Ledger> findByGroupId(String groupId) {

        String sql = "SELECT * FROM ledgers WHERE parent_group_id = ?";
        List<Ledger> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ledgers by group", e);
        }
    }

    @Override
    public List<Ledger> findAll() {

        String sql = "SELECT * FROM ledgers";
        List<Ledger> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all ledgers", e);
        }
    }

    private Ledger mapRow(ResultSet rs) throws SQLException {

        return new Ledger(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("parent_group_id"),
                rs.getDouble("opening_balance"),
                Type.valueOf(rs.getString("type")),
                rs.getString("company_id")
        );
    }
}