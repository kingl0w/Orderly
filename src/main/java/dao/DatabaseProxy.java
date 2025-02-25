package main.java.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * databaseproxy implements the proxy pattern for database operations.
 * currently simulates database functionality for future implementation.
 * 
 * future enhancements:
 * - implement actual database connection
 * - add connection pooling
 * - include transaction management
 */

//proxy pattern for future database implementation
public class DatabaseProxy {
    private static DatabaseProxy instance;
    private final Map<String, Object> storage;

    private DatabaseProxy() {
        storage = new HashMap<>();
    }

    public static DatabaseProxy getInstance() {
        if (instance == null) {
            instance = new DatabaseProxy();
        }
        return instance;
    }

    public void save(String key, Object value) {
        storage.put(key, value);
    }

    public Object get(String key) {
        return storage.get(key);
    }

    public void delete(String key) {
        storage.remove(key);
    }

    public boolean exists(String key) {
        return storage.containsKey(key);
    }
}
