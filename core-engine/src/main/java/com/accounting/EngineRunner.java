package com.accounting;

import com.accounting.application.services.CompanyService;
import com.accounting.domain.enums.Nature;
import com.accounting.domain.models.Company;
import com.accounting.domain.models.Group;
import com.accounting.domain.models.Ledger;
import com.accounting.domain.repository.CompanyRepo;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.domain.repository.LedgerRepo;
import com.accounting.infrastructure.db.DatabaseInitializer;
import com.accounting.infrastructure.db.DatabaseManager;
import com.accounting.infrastructure.persistence.SQLiteCompanyRepository;
import com.accounting.infrastructure.persistence.SQLiteGroupRepository;
import com.accounting.infrastructure.persistence.SQLiteLedgerRepository;

import java.util.*;
import java.util.stream.Collectors;

public class EngineRunner {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();
        Scanner scanner = new Scanner(System.in);

        CompanyRepo companyRepo = new SQLiteCompanyRepository();
        GroupRepo groupRepo = new SQLiteGroupRepository();
        LedgerRepo ledgerRepo = new SQLiteLedgerRepository();

// 👉 Use service (IMPORTANT)
        CompanyService companyService = new CompanyService(companyRepo, groupRepo, ledgerRepo);

// 1. Fetch all companies
        List<Company> companies = companyRepo.findAll();

// 2. Show menu
        System.out.println("\n===== Company Selection =====");

        for (int i = 0; i < companies.size(); i++) {
            System.out.println((i + 1) + ". " + companies.get(i).getName());
        }

        System.out.println("0. Create New Company");

// 3. Take input safely
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input");
            return;
        }

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Company selectedCompany;

// 4. Create new company
        if (choice == 0) {

            System.out.print("Enter new company name: ");
            String name = scanner.nextLine();

            if (name.isBlank()) {
                System.out.println("Company name cannot be empty");
                return;
            }

            selectedCompany = companyService.createCompany(name);

            System.out.println("Company created: " + selectedCompany.getName());

        } else {

            if (choice < 1 || choice > companies.size()) {
                System.out.println("Invalid choice");
                return;
            }

            selectedCompany = companies.get(choice - 1);
        }

        String companyId = selectedCompany.getId();

        System.out.println("\nSelected Company: " + selectedCompany.getName());

        // 4. Fetch data
        List<Group> groups = groupRepo.findByCompanyId(companyId);
        List<Ledger> ledgers = ledgerRepo.findByCompanyId(companyId);

        // Build group tree
        Map<String, List<Group>> groupTree = new HashMap<>();

        for (Group g : groups) {
            String parent = g.getParentGroupId();
            groupTree.computeIfAbsent(parent, k -> new ArrayList<>()).add(g);
        }

        // Map group → ledgers
        Map<String, List<Ledger>> ledgerMap = ledgers.stream()
                .collect(Collectors.groupingBy(Ledger::getParentGroup));

        // 5. Print
        printByNature("Assets", Nature.ASSETS, groups, groupTree, ledgerMap);
        printByNature("Liabilities", Nature.LIABILITY, groups, groupTree, ledgerMap);
        printByNature("Expenses", Nature.EXPENSE, groups, groupTree, ledgerMap);
        printByNature("Income", Nature.INCOME, groups, groupTree, ledgerMap);
    }

    private static void printByNature(
            String title,
            Nature nature,
            List<Group> allGroups,
            Map<String, List<Group>> groupTree,
            Map<String, List<Ledger>> ledgerMap
    ) {

        System.out.println("\n" + title);

        List<Group> roots = allGroups.stream()
                .filter(g -> g.getParentGroupId() == null && g.getNature() == nature)
                .toList();

        for (Group root : roots) {
            printGroup(root, groupTree, ledgerMap, 1);
        }
    }

    private static void printGroup(
            Group group,
            Map<String, List<Group>> groupTree,
            Map<String, List<Ledger>> ledgerMap,
            int level
    ) {

        printIndent(level);
        System.out.println(group.getName());

        // print ledgers
        List<Ledger> ledgers = ledgerMap.getOrDefault(group.getId(), List.of());
        for (Ledger l : ledgers) {
            printIndent(level + 1);
            System.out.println(l.getName());
        }

        // print children
        List<Group> children = groupTree.getOrDefault(group.getId(), List.of());
        for (Group child : children) {
            printGroup(child, groupTree, ledgerMap, level + 1);
        }
    }

    private static void printIndent(int level) {
        System.out.print("  ".repeat(level));
    }
}