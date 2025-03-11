package main.java;

import main.java.ui.SwingUI;
import main.java.util.LoggerUtil;
import main.java.util.DotEnv;
//add the missing JOptionPane import:
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        //load environment variables first
        DotEnv.load();
        
        LoggerUtil logger = LoggerUtil.getInstance();
        logger.info("Starting Orderly Management System");
        
        try {
            SwingUI ui = new SwingUI();
            ui.setVisible(true); 
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
