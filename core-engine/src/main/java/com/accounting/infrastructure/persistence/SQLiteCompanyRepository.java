package com.accounting.infrastructure.persistence;

import com.accounting.domain.models.Company;
import com.accounting.domain.repository.CompanyRepo;
import com.accounting.infrastructure.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteCompanyRepository implements CompanyRepo {

    @Override
    public void save(Company company) {

        String sql = "INSERT INTO companies (id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getId());
            stmt.setString(2, company.getName());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save company", e);
        }
    }

    @Override
    public Optional<Company> findById(String id) {

        String sql = "SELECT * FROM companies WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(
                        new Company(
                                rs.getString("id"),
                                rs.getString("name")
                        )
                );
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch company", e);
        }
    }

    @Override
    public List<Company> findAll() {

        String sql = "SELECT * FROM companies";
        List<Company> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Company(
                        rs.getString("id"),
                        rs.getString("name")
                ));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch companies", e);
        }
    }
}