package com.accounting.infrastructure.persistence;

import com.accounting.domain.enums.Nature;
import com.accounting.domain.models.Group;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.infrastructure.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteGroupRepository implements GroupRepo {

    @Override
    public void save(Group group) {

        String sql = """
            INSERT INTO groups (id, name, nature, parent_group_id, company_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, group.getId());
            stmt.setString(2, group.getName());
            stmt.setString(3, group.getNature().name());
            stmt.setString(4, group.getParentGroupId());
            stmt.setString(5, group.getCompanyId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save group", e);
        }
    }

    @Override
    public Optional<Group> findById(String id) {

        String sql = "SELECT * FROM groups WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch group", e);
        }
    }

    @Override
    public List<Group> findByCompanyId(String companyId) {

        String sql = "SELECT * FROM groups WHERE company_id = ?";
        List<Group> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch groups by company", e);
        }
    }

    @Override
    public List<Group> findAll() {

        String sql = "SELECT * FROM groups";
        List<Group> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch groups", e);
        }
    }

    @Override
    public Optional<Group> findByNameAndCompany(String groupName, String companyId) {
        String sql = "SELECT * FROM groups WHERE company_id = ? and name = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyId);
            stmt.setString(2, groupName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch group", e);
        }
    }

    private Group mapRow(ResultSet rs) throws SQLException {

        return new Group(
                rs.getString("id"),
                rs.getString("name"),
                Nature.valueOf(rs.getString("nature")),
                rs.getString("parent_group_id"),
                rs.getString("company_id")
        );
    }
}