package main.java.dao;

import main.java.model.Customer;
import main.java.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * customerdao handles data access operations for customers.
 * implements singleton pattern for centralized access.
 * 
 * future enhancements:
 * - add caching mechanism
 * - implement advanced search
 * - add customer grouping/categorization
 */
public class CustomerDAO {
    private static CustomerDAO instance;
    private final DatabaseManager dbManager;
    private final LoggerUtil logger;
    
    private CustomerDAO() {
        dbManager = DatabaseManager.getInstance();
        logger = LoggerUtil.getInstance();
        logger.info("CustomerDAO initialized");
    }
    
    public static CustomerDAO getInstance() {
        if (instance == null) {
            instance = new CustomerDAO();
        }
        return instance;
    }
    
    public void save(Customer customer) {
        String sql = "INSERT INTO customers (id, name, email) VALUES (?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getEmail());
            
            pstmt.executeUpdate();
            logger.info("Customer saved: " + customer.getId());
            
        } catch (SQLException e) {
            logger.exception("Error saving customer", e);
            throw new RuntimeException("Error saving customer: " + e.getMessage(), e);
        }
    }
    
    public Customer findById(String id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email")
                    );
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding customer by ID", e);
            throw new RuntimeException("Error finding customer: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding all customers", e);
            throw new RuntimeException("Error retrieving customers: " + e.getMessage(), e);
        }
        
        return customers;
    }
    
    public List<Customer> findByName(String name) {
        String sql = "SELECT * FROM customers WHERE LOWER(name) LIKE ?";
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name.toLowerCase() + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email")
                    ));
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding customers by name", e);
            throw new RuntimeException("Error searching customers: " + e.getMessage(), e);
        }
        
        return customers;
    }
    
    public void update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getId());
            
            pstmt.executeUpdate();
            logger.info("Customer updated: " + customer.getId());
            
        } catch (SQLException e) {
            logger.exception("Error updating customer", e);
            throw new RuntimeException("Error updating customer: " + e.getMessage(), e);
        }
    }
    
    public void delete(String id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            logger.info("Customer deleted: " + id);
            
        } catch (SQLException e) {
            logger.exception("Error deleting customer", e);
            throw new RuntimeException("Error deleting customer: " + e.getMessage(), e);
        }
    }
}