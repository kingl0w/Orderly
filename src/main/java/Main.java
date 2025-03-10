package main.java;

import main.java.ui.SwingUI;
import main.java.util.LoggerUtil;
import main.java.util.DotEnv;
// Add the missing JOptionPane import:
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        // Load environment variables first
        DotEnv.load();
        
        LoggerUtil logger = LoggerUtil.getInstance();
        logger.info("Starting Orderly Management System");
        
        try {
            SwingUI ui = new SwingUI();
            ui.setVisible(true); // Show the Swing UI
        } catch (Exception e) {
            logger.exception("Unhandled exception in application", e);
            JOptionPane.showMessageDialog(
                null, 
                "An error occurred: " + e.getMessage(), 
                "Application Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
