package com.accounting;

import com.accounting.cli.MainMenu;
import com.accounting.infrastructure.db.DatabaseInitializer;

public class EngineRunner {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        MainMenu menu = new MainMenu();
        menu.start();
    }
}