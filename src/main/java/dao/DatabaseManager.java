package main.java.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import main.java.util.LoggerUtil;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final LoggerUtil logger;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/orderly";
    private static final String USER = DBUSER;
    private static final String PASS = DBPASSWORD;
    
    private DatabaseManager() {
        logger = LoggerUtil.getInstance();
        
        try {
            // Ensure the driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC driver loaded successfully");
            
            // Initialize database
            try (Connection conn = getConnection()) {
                initDatabase(conn);
                logger.info("Database initialized successfully");
            }
        } catch (ClassNotFoundException e) {
            logger.exception("MySQL JDBC driver not found", e);
            throw new RuntimeException("MySQL JDBC driver not found", e);
        } catch (SQLException e) {
            logger.exception("Error initializing database", e);
            throw new RuntimeException("Error initializing database", e);
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
    
    private void initDatabase(Connection conn) throws SQLException {
        createProductTable(conn);
        createCustomerTable(conn);
        createOrderTable(conn);
        createOrderItemTable(conn);
    }
    
    private void createProductTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "price DECIMAL(10,2) NOT NULL, " +
                "description TEXT, " +
                "stock INT NOT NULL" +
                ")";
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private void createCustomerTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS customers (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL" +
                ")";
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private void createOrderTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS orders (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "customer_id VARCHAR(50) NOT NULL, " +
                "order_date TIMESTAMP NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "FOREIGN KEY (customer_id) REFERENCES customers(id)" +
                ")";
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private void createOrderItemTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS order_items (" +
                "order_id VARCHAR(50) NOT NULL, " +
                "product_id VARCHAR(50) NOT NULL, " +
                "quantity INT NOT NULL, " +
                "PRIMARY KEY (order_id, product_id), " +
                "FOREIGN KEY (order_id) REFERENCES orders(id), " +
                "FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")";
        
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
