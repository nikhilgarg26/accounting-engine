package com.accounting.application.services;

import com.accounting.domain.enums.Nature;
import com.accounting.domain.models.Group;
import com.accounting.domain.repository.GroupRepo;

import java.util.List;
import java.util.Optional;

public class GroupService {

    private final GroupRepo groupRepo;

    public GroupService(GroupRepo groupRepo) {
        this.groupRepo = groupRepo;
    }

    public Group createGroup(String name, String parentGroupId, String companyId) {

        // 1. Basic validation
        Nature nature = null;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        // 2. Prevent duplicate group names per company
        List<Group> existing = groupRepo.findByCompanyId(companyId);
        boolean duplicate = existing.stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(name));

        if (duplicate) {
            throw new RuntimeException("Group with same name already exists");
        }

        // 3. Validate parent group (if exists)
        if (parentGroupId != null) {
            Group parent = groupRepo.findById(parentGroupId)
                    .orElseThrow(() -> new RuntimeException("Parent group not found"));

            // 4. Same company validation
            if (!parent.getCompanyId().equals(companyId)) {
                throw new RuntimeException("Parent group belongs to different company");
            }

            // 5. Nature consistency
            nature = parent.getNature();
        }

        Group group = new Group(name, nature, parentGroupId, companyId);
        groupRepo.save(group);

        return group;
    }

    public List<Group> getGroupsByCompany(String companyId) {
        return groupRepo.findByCompanyId(companyId);
    }

    public Optional<Group> getGroup(String id) {
        return groupRepo.findById(id);
    }
}