package main.java.dao;

import main.java.model.*;
import main.java.util.LoggerUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * orderdao handles data access operations for orders.
 * implements singleton pattern for centralized data access.
 * 
 * future enhancements:
 * - implement database storage
 * - add batch operations
 * - implement transaction handling
 */
public class OrderDAO {
    private static OrderDAO instance;
    private final DatabaseManager dbManager;
    private final LoggerUtil logger;
    private final CustomerDAO customerDAO;
    private final ProductDAO productDAO;
    
    private OrderDAO() {
        dbManager = DatabaseManager.getInstance();
        logger = LoggerUtil.getInstance();
        customerDAO = CustomerDAO.getInstance();
        productDAO = ProductDAO.getInstance();
        logger.info("OrderDAO initialized");
    }
    
    public static OrderDAO getInstance() {
        if (instance == null) {
            instance = new OrderDAO();
        }
        return instance;
    }
    
    public void save(Order order) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);
            
            //save order header
            String orderSql = "INSERT INTO orders (id, customer_id, order_date, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setString(1, order.getId());
                pstmt.setString(2, order.getCustomer().getId());
                pstmt.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
                pstmt.setString(4, order.getStatus().toString());
                pstmt.executeUpdate();
            }
            
            //save order items
            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    pstmt.setString(1, order.getId());
                    pstmt.setString(2, item.getProduct().getId());
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.executeUpdate();
                }
            }
            
            conn.commit();
            logger.info("Order saved: " + order.getId());
            
        } catch (SQLException e) {
            logger.exception("Error saving order", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.exception("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Error saving order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.exception("Error closing connection", e);
                }
            }
        }
    }
    
    public Order findById(String id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    loadOrderItems(order);
                    return order;
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding order by ID", e);
            throw new RuntimeException("Error finding order: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding all orders", e);
            throw new RuntimeException("Error retrieving orders: " + e.getMessage(), e);
        }
        
        return orders;
    }
    
    public List<Order> findByCustomerId(String customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding orders by customer ID", e);
            throw new RuntimeException("Error searching orders: " + e.getMessage(), e);
        }
        
        return orders;
    }
    
    public List<Order> findByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ?";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding orders by date range", e);
            throw new RuntimeException("Error searching orders: " + e.getMessage(), e);
        }
        
        return orders;
    }
    
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, order.getStatus().toString());
            pstmt.setString(2, order.getId());
            
            pstmt.executeUpdate();
            logger.info("Order updated: " + order.getId());
            
        } catch (SQLException e) {
            logger.exception("Error updating order", e);
            throw new RuntimeException("Error updating order: " + e.getMessage(), e);
        }
    }
    
    public void delete(String id) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);
            
            // Delete order items first
            String deleteItemsSql = "DELETE FROM order_items WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteItemsSql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
            
            // Then delete order
            String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteOrderSql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            logger.info("Order deleted: " + id);
            
        } catch (SQLException e) {
            logger.exception("Error deleting order", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.exception("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Error deleting order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.exception("Error closing connection", e);
                }
            }
        }
    }
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String customerId = rs.getString("customer_id");
        OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
        
        Customer customer = customerDAO.findById(customerId);
        Order order = new Order(id, customer);
        order.setStatus(status);
        
        return order;
    }
    
    private void loadOrderItems(Order order) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, order.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    int quantity = rs.getInt("quantity");
                    
                    Product product = productDAO.findById(productId);
                    if (product != null) {
                        order.addItem(product, quantity);
                    }
                }
            }
        }
    }
}