package com.accounting.cli;

import com.accounting.application.services.*;
import com.accounting.domain.enums.Type;
import com.accounting.domain.enums.VoucherTypes;
import com.accounting.domain.models.*;
import com.accounting.domain.repository.*;
import com.accounting.infrastructure.persistence.*;

import java.util.List;
import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    private final CompanyRepo companyRepo = new SQLiteCompanyRepository();
    private final GroupRepo groupRepo = new SQLiteGroupRepository();
    private final LedgerRepo ledgerRepo = new SQLiteLedgerRepository();
    private final VoucherRepo voucherRepo = new SQLiteVoucherRepository();

    private final CompanyService companyService =
            new CompanyService(companyRepo, groupRepo, ledgerRepo);

    private final VoucherService voucherService =
            new VoucherService(voucherRepo);

    private final ReportService reportService = new ReportService(voucherRepo, ledgerRepo, groupRepo);

    private Company selectedCompany;

    public void start() {

        while (true) {

            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Create Company");
            System.out.println("2. Select Company");
            System.out.println("3. Create Group");
            System.out.println("4. Create Ledger");
            System.out.println("5. Create Voucher");
            System.out.println("6. View Groups");
            System.out.println("7. View Ledgers");
            System.out.println("8. View Vouchers");
            System.out.println("9. View Group Details");
            System.out.println("10. View Ledger Details");
            System.out.println("11. View Trial Balance");
            System.out.println("12. View Profit & Loss");
            System.out.println("13. View Balance Sheet");
            System.out.println("14. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> createCompany();
                case 2 -> selectCompany();
                case 3 -> createGroup();
                case 4 -> createLedger();
                case 5 -> createVoucher();
                case 6 -> viewGroups();
                case 7 -> viewLedgers();
                case 8 -> viewVouchers();
                case 9 -> viewGroupDetails();
                case 10 -> viewLedgerDetails();
                case 11 -> viewTrialBalance();
                case 12 -> viewProfitLoss();
                case 13 -> viewBalanceSheet();
                case 14 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    // ---------------- COMPANY ----------------

    private void createCompany() {
        System.out.print("Enter company name: ");
        String name = scanner.nextLine();

        Company c = companyService.createCompany(name);
        System.out.println("Created: " + c.getName());
    }

    private void selectCompany() {

        List<Company> companies = companyRepo.findAll();

        if (companies.isEmpty()) {
            System.out.println("No companies found.");
            return;
        }

        for (int i = 0; i < companies.size(); i++) {
            System.out.println((i + 1) + ". " + companies.get(i).getName());
        }

        int choice = scanner.nextInt();
        scanner.nextLine();

        selectedCompany = companies.get(choice - 1);

        System.out.println("Selected: " + selectedCompany.getName());
    }

    // ---------------- GROUP ----------------

    private void createGroup() {

        if (!checkCompany()) return;

        System.out.print("Enter group name: ");
        String name = scanner.nextLine();

        System.out.print("Enter parent group ID (or blank): ");
        String parent = scanner.nextLine();

        groupRepo.save(new com.accounting.domain.models.Group(
                name,
                com.accounting.domain.enums.Nature.ASSETS,
                parent.isBlank() ? null : parent,
                selectedCompany.getId()
        ));

        System.out.println("Group created");
    }

    // ---------------- LEDGER ----------------

    private void createLedger() {

        if (!checkCompany()) return;

        System.out.print("Enter ledger name: ");
        String name = scanner.nextLine();

        List<Group> groups = groupRepo.findByCompanyId(selectedCompany.getId());

        if (groups.isEmpty()) {
            System.out.println("No groups found.");
            return;
        }

        System.out.println("Select Group:");

        for (int i = 0; i < groups.size(); i++) {
            System.out.println((i + 1) + ". " + groups.get(i).getName());
        }

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > groups.size()) {
            System.out.println("Invalid choice");
            return;
        }

        Group selectedGroup = groups.get(choice - 1);

        ledgerRepo.save(new Ledger(
                name,
                selectedGroup.getId(),
                0,
                Type.DR,
                selectedCompany.getId()
        ));

        System.out.println("Ledger created under: " + selectedGroup.getName());
    }

    // ---------------- VOUCHER ----------------

    private void createVoucher() {

        if (!checkCompany()) return;

        System.out.println("\nSelect Voucher Type:");
        System.out.println("1. Payment");
        System.out.println("2. Receipt");
        System.out.println("3. Sales");
        System.out.println("4. Purchase");

        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        Ledger drLedger = null;
        Ledger crLedger = null;
        VoucherTypes type = VoucherTypes.JOURNAL;

        switch (typeChoice) {

            case 1 -> { // PAYMENT
                Ledger cash = selectLedger("Select Account (Cash/Bank):");
                Ledger expense = selectLedger("Select Particular (Expense / Party):");


                drLedger = expense;
                crLedger = cash;
                type = VoucherTypes.PAYMENT;
            }

            case 2 -> { // RECEIPT
                Ledger cash = selectLedger("Select Account (Cash/Bank):");
                Ledger party = selectLedger("Select Particular (Party / Income):");

                drLedger = cash;
                crLedger = party;
                type = VoucherTypes.RECEIPT;
            }

            case 3 -> { // SALES
                Ledger party = selectLedger("Select Party (Customer):");
                Ledger sales = selectLedger("Select Sales Ledger:");

                drLedger = party;
                crLedger = sales;
                type = VoucherTypes.SALES;
            }

            case 4 -> { // PURCHASE
                Ledger purchase = selectLedger("Select Purchase Ledger:");
                Ledger supplier = selectLedger("Select Supplier:");

                drLedger = purchase;
                crLedger = supplier;
                type = VoucherTypes.PURCHASE;
            }

            default -> {
                System.out.println("Invalid choice");
                return;
            }
        }

        Entry e1 = new Entry(drLedger.getId(), amount, Type.DR);
        Entry e2 = new Entry(crLedger.getId(), amount, Type.CR);

        Voucher v = voucherService.createVoucher(
                selectedCompany.getId(),
                type,
                java.time.LocalDate.now(),
                List.of(e1, e2)
        );

        System.out.println("Voucher created: " + v.getId());
    }

    // ---------------- VIEW ----------------

    private void viewGroups() {
        if (!checkCompany()) return;

        groupRepo.findByCompanyId(selectedCompany.getId())
                .forEach(g -> System.out.println(g.getName()));
    }

    private void viewLedgers() {
        if (!checkCompany()) return;

        ledgerRepo.findByCompanyId(selectedCompany.getId())
                .forEach(l -> System.out.println(l.getName()));
    }

    private void viewVouchers() {
        System.out.println("Implement using VoucherService");
    }

    private void viewGroupDetails() {

        if (!checkCompany()) return;

        List<Group> groups = groupRepo.findByCompanyId(selectedCompany.getId());

        if (groups.isEmpty()) {
            System.out.println("No groups found.");
            return;
        }

        System.out.println("Select Group:");

        for (int i = 0; i < groups.size(); i++) {
            System.out.println((i + 1) + ". " + groups.get(i).getName());
        }

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > groups.size()) {
            System.out.println("Invalid choice");
            return;
        }

        Group g = groups.get(choice - 1);

        System.out.println("\n===== GROUP DETAILS =====");
        System.out.println("Name: " + g.getName());
        System.out.println("Nature: " + g.getNature());
        String parentId = g.getParentGroupId();

        if (parentId == null) {
            System.out.println("Parent Group: None (Root Group)");
        } else {
            Group parent = groupRepo.findById(parentId).orElse(null);

            System.out.println("Parent Group: " +
                    (parent != null ? parent.getName() : "Unknown"));
        }
        System.out.println("Company ID: " + g.getCompanyId());

    }

    private void viewLedgerDetails() {

        if (!checkCompany()) return;

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(selectedCompany.getId());

        if (ledgers.isEmpty()) {
            System.out.println("No ledgers found.");
            return;
        }

        System.out.println("Select Ledger:");

        for (int i = 0; i < ledgers.size(); i++) {
            System.out.println((i + 1) + ". " + ledgers.get(i).getName());
        }

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > ledgers.size()) {
            System.out.println("Invalid choice");
            return;
        }

        Ledger l = ledgers.get(choice - 1);

        Group group = groupRepo.findById(l.getParentGroup()).orElse(null);



        System.out.println("\n===== LEDGER DETAILS =====");
        System.out.println("Name: " + l.getName());
        System.out.println("Group: " + (group != null ? group.getName() : "Unknown"));

        String balance = reportService.getLedgerBalanceFormatted(l.getId());
        System.out.println("Balance: " + balance);
        System.out.println("Type: " + l.getType());
        System.out.println("Company ID: " + l.getCompanyId());
    }

    private void viewTrialBalance() {

        if (!checkCompany()) return;

        List<TrialBalanceRow> rows =
                reportService.getTrialBalance(selectedCompany.getId());

        double totalDr = 0;
        double totalCr = 0;

        System.out.println("\n===== TRIAL BALANCE =====");
        System.out.printf("%-20s %-10s %-10s%n", "Ledger", "DR", "CR");

        for (TrialBalanceRow row : rows) {

            System.out.printf("%-20s %-10.2f %-10.2f%n",
                    row.getLedgerName(),
                    row.getDebit(),
                    row.getCredit()
            );

            totalDr += row.getDebit();
            totalCr += row.getCredit();
        }

        System.out.println("-------------------------------------------");

        System.out.printf("%-20s %-10.2f %-10.2f%n",
                "TOTAL", totalDr, totalCr);

        if (totalDr == totalCr) {
            System.out.println("✅ Trial Balance Matched");
        } else {
            System.out.println("❌ Trial Balance NOT Matching");
        }
    }

    private void viewProfitLoss() {

        if (!checkCompany()) return;

        ProfitLossStatement pl =
                reportService.getProfitLoss(selectedCompany.getId());

        System.out.println("\n===== PROFIT & LOSS =====");

        System.out.println("\n--- Income ---");
        pl.getIncome().forEach((name, amt) ->
                System.out.printf("%-20s %.2f%n", name, amt));

        System.out.println("Total Income: " + pl.getTotalIncome());

        System.out.println("\n--- Expense ---");
        pl.getExpense().forEach((name, amt) ->
                System.out.printf("%-20s %.2f%n", name, amt));

        System.out.println("Total Expense: " + pl.getTotalExpense());

        System.out.println("\n-------------------------");

        if (pl.getProfit() >= 0) {
            System.out.println("Profit: " + pl.getProfit());
        } else {
            System.out.println("Loss: " + Math.abs(pl.getProfit()));
        }
    }

    private void viewBalanceSheet() {

        if (!checkCompany()) return;

        BalanceSheet bs =
                reportService.getBalanceSheet(selectedCompany.getId());

        System.out.println("\n===== BALANCE SHEET =====");

        System.out.println("\n--- Assets ---");
        bs.getAssets().forEach((name, amt) ->
                System.out.printf("%-20s %.2f%n", name, amt));

        System.out.println("Total Assets: " + bs.getTotalAssets());

        System.out.println("\n--- Liabilities ---");
        bs.getLiabilities().forEach((name, amt) ->
                System.out.printf("%-20s %.2f%n", name, amt));

        System.out.println("Total Liabilities: " + bs.getTotalLiabilities());

        System.out.println("\n-------------------------");

        if (bs.getTotalAssets() == bs.getTotalLiabilities()) {
            System.out.println("✅ Balance Sheet Matched");
        } else {
            System.out.println("❌ Balance Sheet NOT Matching");
        }
    }



    // ---------------- UTIL ----------------

    private boolean checkCompany() {
        if (selectedCompany == null) {
            System.out.println("Select company first!");
            return false;
        }
        return true;
    }


    public VoucherRepo getVoucherRepo() {
        return voucherRepo;
    }

    private Ledger
    selectLedger(String message) {

        List<Ledger> ledgers = ledgerRepo.findByCompanyId(selectedCompany.getId());

        System.out.println("\n" + message);

        for (int i = 0; i < ledgers.size(); i++) {
            System.out.println((i + 1) + ". " + ledgers.get(i).getName());
        }

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > ledgers.size()) {
            throw new RuntimeException("Invalid ledger selection");
        }

        return ledgers.get(choice - 1);
    }
}