package com.accounting;

import com.accounting.application.services.CompanyService;
import com.accounting.application.services.VoucherService;
import com.accounting.cli.MainMenu;
import com.accounting.domain.repository.CompanyRepo;
import com.accounting.domain.repository.GroupRepo;
import com.accounting.domain.repository.LedgerRepo;
import com.accounting.domain.repository.VoucherRepo;
import com.accounting.infrastructure.db.DatabaseInitializer;
import com.accounting.infrastructure.persistence.SQLiteCompanyRepository;
import com.accounting.infrastructure.persistence.SQLiteGroupRepository;
import com.accounting.infrastructure.persistence.SQLiteLedgerRepository;
import com.accounting.infrastructure.persistence.SQLiteVoucherRepository;
import com.accounting.util.DataSeeder;

public class EngineRunner {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

//        CompanyRepo companyRepo = new SQLiteCompanyRepository();2

//        GroupRepo groupRepo = new SQLiteGroupRepository();
//        LedgerRepo ledgerRepo = new SQLiteLedgerRepository();
//        VoucherRepo voucherRepo = new SQLiteVoucherRepository();
//
//        CompanyService companyService =
//                new CompanyService(companyRepo, groupRepo, ledgerRepo);
//
//        VoucherService voucherService =
//                new VoucherService(voucherRepo);
//
//        // 🔥 Seed data
//        new DataSeeder(companyService, groupRepo, ledgerRepo, voucherService).seed();

        // Start CLI
        new MainMenu().start();
    }
}