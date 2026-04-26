package com.accounting.domain.repository;

import com.accounting.domain.models.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepo {

    void save(Group group);

    Optional<Group> findById(String id);

    List<Group> findByCompanyId(String companyId);

    List<Group> findAll();
}