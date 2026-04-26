package com.accounting.domain.repository;

import com.accounting.domain.models.Ledger;

import java.util.List;
import java.util.Optional;

public interface LedgerRepo{

    void save(Ledger ledger);

    Optional<Ledger> findById(String id);

    Optional<Ledger> findByNameAndCompany(String name, String companyId);

    List<Ledger> findByCompanyId(String companyId);

    List<Ledger> findByGroupId(String groupId);

    List<Ledger> findAll();
}