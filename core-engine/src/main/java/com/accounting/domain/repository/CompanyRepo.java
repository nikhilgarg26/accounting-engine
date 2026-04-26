package com.accounting.domain.repository;

import com.accounting.domain.models.Company;

import java.util.List;

public interface CompanyRepo {

    void save(Company company);

    Company findById(String id);

    List<Company> findAll();
}