package main.java.dao;

import main.java.model.Order;
import main.java.util.LoggerUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final DatabaseProxy db;
    private final LoggerUtil logger;
    
    private OrderDAO() {
        db = DatabaseProxy.getInstance();
        logger = LoggerUtil.getInstance();
        logger.info("OrderDAO initialized");
    }
    
    public static OrderDAO getInstance() {
        if (instance == null) {
            instance = new OrderDAO();
        }
        return instance;
    }
    
    public void save(Order order) {
        logger.info("Saving order: " + order.getId());
        db.save("order:" + order.getId(), order);
    }
    
    public Order findById(String id) {
        logger.info("Finding order by ID: " + id);
        return (Order) db.get("order:" + id);
    }
    
    public List<Order> findAll() {
        logger.info("Retrieving all orders");
        List<Order> orders = new ArrayList<>();
        
        //for now, return an empty list since there is no way
        //to query all orders from simple database proxy
        return orders;
    }
    
    public List<Order> findByCustomerId(String customerId) {
        logger.info("Finding orders by customer ID: " + customerId);
        List<Order> orders = findAll();
        return orders.stream()
                .filter(order -> order.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
    }
    
    public List<Order> findByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.info("Finding orders between " + start + " and " + end);
        List<Order> orders = findAll();
        return orders.stream()
                .filter(order -> !order.getOrderDate().isBefore(start) && !order.getOrderDate().isAfter(end))
                .collect(Collectors.toList());
    }
    
    public void update(Order order) {
        logger.info("Updating order: " + order.getId());
        db.save("order:" + order.getId(), order);
    }
    
    public void delete(String id) {
        logger.info("Deleting order: " + id);
        db.delete("order:" + id);
    }
}