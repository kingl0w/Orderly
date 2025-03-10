package main.java.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * dotenv utility class for loading environment variables from a .env file.
 * provides a simple way to keep sensitive data like database credentials
 * separate from the code.
 */
public class DotEnv {
    private static Map<String, String> env = new HashMap<>();
    private static boolean initialized = false;
    private static final LoggerUtil logger = LoggerUtil.getInstance();
    
    public static void load() {
        if (initialized) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            logger.info("Loading .env file");
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    int equalIndex = line.indexOf('=');
                    if (equalIndex > 0) {
                        String key = line.substring(0, equalIndex).trim();
                        String value = line.substring(equalIndex + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        env.put(key, value);
                        logger.info("Loaded environment variable: " + key);
                    }
                }
            }
            initialized = true;
            logger.info(".env file loaded successfully");
        } catch (IOException e) {
            logger.warning("Error loading .env file: " + e.getMessage());
            logger.info("Continuing with default values");
        }
    }
    
    public static String get(String key) {
        if (!initialized) {
            load();
        }
        return env.get(key);
    }
    
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
