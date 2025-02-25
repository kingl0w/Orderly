package main.java;

import main.java.ui.ConsoleUI;
import main.java.util.LoggerUtil;

/**
 * main application class that initializes and starts the order management system.
 * acts as the entry point for the console-based application.
 * 
 * future enhancements:
 * - add configuration loading
 * - implement login system
 */
public class Main {
    public static void main(String[] args) {
        LoggerUtil logger = LoggerUtil.getInstance();
        logger.info("Starting Orderly Management System");
        
        try {
            ConsoleUI ui = new ConsoleUI();
            ui.start();
        } catch (Exception e) {
            logger.exception("Unhandled exception in application", e);
        }
    }
}
