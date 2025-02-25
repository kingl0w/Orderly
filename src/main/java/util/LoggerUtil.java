package main.java.util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * logger utility class provides centralized logging capabilities for the application.
 * implements both file and console logging with configurable levels.
 * 
 * future enhancements:
 * - add custom formatter
 * - implement log rotation
 * - add log filtering options
 */
public class LoggerUtil {
    private static final String LOG_FILE = "orderly.log";
    private static LoggerUtil instance;
    private final Logger logger;
    
    private LoggerUtil() {
        //creates the logger
        logger = Logger.getLogger("main.java.OrderlyLogger");
        
        try {
            //sets global level (will be inherited by handlers)
            logger.setLevel(Level.INFO);
            
            //removes existing handlers to avoid duplication
            Logger rootLogger = Logger.getLogger("");
            for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }
            
            //create console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
            
            //create file handler
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(Level.ALL);
            
            //use simple formatter instead of default XML formatter
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            
            logger.addHandler(fileHandler);
            
            //don't send logs to parent handlers
            logger.setUseParentHandlers(false);
            
        } catch (IOException e) {
            System.err.println("Error setting up logger: " + e.getMessage());
        }
    }
    
    public static synchronized LoggerUtil getInstance() {
        if (instance == null) {
            instance = new LoggerUtil();
        }
        return instance;
    }
    
    public void info(String message) {
        logger.info(message);
    }
    
    public void warning(String message) {
        logger.warning(message);
    }
    
    public void severe(String message) {
        logger.severe(message);
    }
    
    public void debug(String message) {
        logger.fine(message);
    }
    
    public void exception(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
    }
}
