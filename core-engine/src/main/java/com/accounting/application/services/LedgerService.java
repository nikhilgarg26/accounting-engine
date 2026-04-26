package com.accounting.application.services;


import com.accounting.domain.enums.Type;
import com.accounting.domain.models.Group;
import com.accounting.domain.models.Ledger;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.domain.repository.LedgerRepo;

import java.util.List;

public class LedgerService {

    private final LedgerRepo ledgerRepo;
    private final GroupRepo groupRepo;

    public LedgerService(LedgerRepo ledgerRepo, GroupRepo groupRepo) {
        this.ledgerRepo = ledgerRepo;
        this.groupRepo = groupRepo;
    }

    public Ledger createLedger(
            String name,
            String groupId,
            double openingBalance,
            Type type,
            String companyId
    ) {

        if (name == null || name.isBlank()) {
            throw new RuntimeException("Ledger name cannot be empty");
        }

        // duplicate check
        ledgerRepo.findByNameAndCompany(name, companyId)
                .ifPresent(l -> {
                    throw new RuntimeException("Ledger already exists");
                });

        // group validation
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Group belongs to different company");
        }

        // opening balance validation
        if (openingBalance < 0) {
            throw new RuntimeException("Opening balance cannot be negative");
        }

        if (openingBalance > 0 && type == null) {
            throw new RuntimeException("Type required (DR/CR)");
        }

        Ledger ledger = new Ledger(
                name,
                groupId,
                openingBalance,
                type,
                companyId
        );

        ledgerRepo.save(ledger);

        return ledger;
    }

    public Ledger getLedgerByName(String name, String companyId) {
        return ledgerRepo.findByNameAndCompany(name, companyId)
                .orElseThrow(() -> new RuntimeException("Ledger not found"));
    }

    public List<Ledger> getLedgersByCompany(String companyId) {
        return ledgerRepo.findByCompanyId(companyId);
    }

    public List<Ledger> getLedgersByGroup(String groupId) {
        return ledgerRepo.findByGroupId(groupId);
    }
}

