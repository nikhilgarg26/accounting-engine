package com.accounting.domain.repository;

import com.accounting.domain.models.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepo {

    void save(Company company);

    Optional<Company> findById(String id);

    List<Company> findAll();
}