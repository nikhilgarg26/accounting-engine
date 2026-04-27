package com.accounting.util;

import com.accounting.application.services.CompanyService;
import com.accounting.application.services.VoucherService;
import com.accounting.domain.enums.Type;
import com.accounting.domain.enums.VoucherTypes;
import com.accounting.domain.models.*;
import com.accounting.domain.repository.*;

import java.time.LocalDate;
import java.util.*;

public class DataSeeder {

    private final CompanyService companyService;
    private final GroupRepo groupRepo;
    private final LedgerRepo ledgerRepo;
    private final VoucherService voucherService;

    public DataSeeder(CompanyService companyService,
                      GroupRepo groupRepo,
                      LedgerRepo ledgerRepo,
                      VoucherService voucherService) {
        this.companyService = companyService;
        this.groupRepo = groupRepo;
        this.ledgerRepo = ledgerRepo;
        this.voucherService = voucherService;
    }

    public void seed() {

        Company company = companyService.createCompany("new Company");
        String companyId = company.getId();

        System.out.println("Seeding data...");

        // ---------------- CREATE LEDGERS ----------------

        Ledger cash = getOrCreate("Cash", "Cash-in-hand", companyId);

        Ledger sales = getOrCreate("Sales", "Sales Accounts", companyId);
        Ledger purchase = getOrCreate("Purchase", "Purchase Accounts", companyId);

        Ledger rent = getOrCreate("Rent", "Direct Expenses", companyId);
        Ledger electricity = getOrCreate("Electricity", "Direct Expenses", companyId);

        Ledger customer1 = getOrCreate("Customer A", "Sundry Debtors", companyId);
        Ledger customer2 = getOrCreate("Customer B", "Sundry Debtors", companyId);

        Ledger supplier1 = getOrCreate("Supplier A", "Sundry Creditors", companyId);
        Ledger supplier2 = getOrCreate("Supplier B", "Sundry Creditors", companyId);

        List<Ledger> customers = List.of(customer1, customer2);
        List<Ledger> suppliers = List.of(supplier1, supplier2);
        List<Ledger> expenses = List.of(rent, electricity);

        Random rand = new Random();

        // ---------------- TRANSACTIONS ----------------

        for (int i = 0; i < 50; i++){

            int type = rand.nextInt(4);
            double amount = 100 + rand.nextInt(1000);

            Ledger dr = null;
            Ledger cr = null;
            VoucherTypes voucherType;

            switch (type) {

                case 0 -> { // SALES
                    Ledger customer = customers.get(rand.nextInt(customers.size()));
                    dr = customer;
                    cr = sales;
                    voucherType = VoucherTypes.SALES;
                }

                case 1 -> { // RECEIPT
                    Ledger customer = customers.get(rand.nextInt(customers.size()));
                    dr = cash;
                    cr = customer;
                    voucherType = VoucherTypes.RECEIPT;
                }

                case 2 -> { // PURCHASE
                    Ledger supplier = suppliers.get(rand.nextInt(suppliers.size()));
                    dr = purchase;
                    cr = supplier;
                    voucherType = VoucherTypes.PURCHASE;
                }

                default -> { // PAYMENT
                    Ledger expense = expenses.get(rand.nextInt(expenses.size()));
                    dr = expense;
                    cr = cash;
                    voucherType = VoucherTypes.PAYMENT;
                }
            }

            voucherService.createVoucher(
                    companyId,
                    voucherType,
                    LocalDate.now(),
                    List.of(
                            new Entry(dr.getId(), amount, Type.DR),
                            new Entry(cr.getId(), amount, Type.CR)
                    )
            );
        }

        System.out.println("✅ 50 transactions created successfully!");
    }

    // ---------------- HELPER ----------------

    private Ledger getOrCreate(String ledgerName, String groupName, String companyId) {

        return ledgerRepo.findByNameAndCompany(ledgerName, companyId)
                .orElseGet(() -> {

                    Group group = groupRepo.findByNameAndCompany(groupName, companyId)
                            .orElseThrow(() ->
                                    new RuntimeException("Group not found: " + groupName));

                    Ledger ledger = new Ledger(
                            ledgerName,
                            group.getId(),
                            0,
                            Type.DR,
                            companyId
                    );

                    ledgerRepo.save(ledger);
                    return ledger;
                });
    }
}