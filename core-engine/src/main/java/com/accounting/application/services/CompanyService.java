package com.accounting.application.services;

import com.accounting.application.defaults.DefaultGroups;
import com.accounting.application.defaults.DefaultLedgers;
import com.accounting.domain.models.Company;
import com.accounting.domain.models.Group;
import com.accounting.domain.models.Ledger;
import com.accounting.domain.repository.CompanyRepo;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.domain.repository.LedgerRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompanyService {

    private final CompanyRepo companyRepo;
    private final GroupRepo groupRepo;
    private final LedgerRepo ledgerRepo;

    public CompanyService(CompanyRepo companyRepo, GroupRepo groupRepo, LedgerRepo ledgerRepo) {
        this.companyRepo = companyRepo;
        this.groupRepo = groupRepo;
        this.ledgerRepo = ledgerRepo;
    }

    public Company createCompany(String name) {

        // 1. Create company
        Company company = new Company(name);
        companyRepo.save(company);

        // 2. Create default groups
        createDefaultGroups(company);
        createDefaultLedgers(company);

        return company;
    }

    private void createDefaultGroups(Company company) {

        Map<String, String> nameToId = new HashMap<>();

        for (DefaultGroups.GroupDef def : DefaultGroups.get()) {

            String parentId = null;

            if (def.parentName != null) {
                parentId = nameToId.get(def.parentName);
            }

            Group group = new Group(
                    def.name,
                    def.nature,
                    parentId,
                    company.getId()
            );

            groupRepo.save(group);

            nameToId.put(def.name, group.getId());
        }
    }

    public void createDefaultLedgers(Company company) {

        List<Group> groups = groupRepo.findByCompanyId(company.getId());

        Map<String, String> groupNameToId = groups.stream()
                .collect(Collectors.toMap(Group::getName, Group::getId));

        for (DefaultLedgers.LedgerDef def : DefaultLedgers.get()) {

            String groupId = groupNameToId.get(def.parentGroup);

            Ledger ledger = new Ledger(
                    def.name,
                    groupId,
                    def.openingBalance,
                    def.type,
                    company.getId()
            );

            ledgerRepo.save(ledger);
        }
    }
}