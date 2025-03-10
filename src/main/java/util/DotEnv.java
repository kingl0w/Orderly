package main.java.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DotEnv {
    private static Map<String, String> env = new HashMap<>();
    private static boolean initialized = false;
    
    public static void load() {
        if (initialized) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    int equalIndex = line.indexOf('=');
                    if (equalIndex > 0) {
                        String key = line.substring(0, equalIndex).trim();
                        String value = line.substring(equalIndex + 1).trim();
                        //remove quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        env.put(key, value);
                    }
                }
            }
            initialized = true;
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
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
