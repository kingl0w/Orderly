package main.java.service;

import main.java.dao.OrderDAO;
import main.java.dao.ProductDAO;
import main.java.exception.InsufficientStockException;
import main.java.exception.InvalidOrderException;
import main.java.exception.ProductNotFoundException;
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.Product;
import main.java.model.OrderStatus;
import main.java.util.LoggerUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * orderservice handles business logic for order operations.
 * manages the creation, editing, and searching of orders.
 * 
 * future enhancements:
 * - add order validation
 * - implement inventory adjustment
 * - add payment processing
 */
public class OrderService {
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final LoggerUtil logger;
    
    public OrderService() {
        this.orderDAO = OrderDAO.getInstance();
        this.productDAO = ProductDAO.getInstance();
        this.logger = LoggerUtil.getInstance();
        logger.info("OrderService initialized");
    }
    
    public void createOrder(Order order) throws InvalidOrderException {
        logger.info("Creating order: " + order.getId());
        
        //validates order
        if (order.getItems().isEmpty()) {
            logger.warning("Attempted to create order with no items: " + order.getId());
            throw new InvalidOrderException("Order must contain at least one item");
        }
        
        try {
            //checks product availability and update stock
            for (OrderItem item : order.getItems()) {
                String productId = item.getProduct().getId();
                int quantity = item.getQuantity();
                
                updateProductStock(productId, -quantity);
            }
            
            //save order
            orderDAO.save(order);
            logger.info("Order created successfully: " + order.getId());
        } catch (Exception e) {
            logger.exception("Error creating order", e);
            throw new InvalidOrderException("Failed to create order: " + e.getMessage());
        }
    }
    
    public Order getOrder(String id) {
        logger.info("Retrieving order: " + id);
        return orderDAO.findById(id);
    }
    
    public List<Order> getAllOrders() {
        logger.info("Retrieving all orders");
        return orderDAO.findAll();
    }
    
    public List<Order> getOrdersByCustomer(String customerId) {
        logger.info("Retrieving orders for customer: " + customerId);
        return orderDAO.findByCustomerId(customerId);
    }
    
    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.info("Retrieving orders between " + start + " and " + end);
        return orderDAO.findByDateRange(start, end);
    }
    
    public void updateOrderStatus(String orderId, OrderStatus newStatus) throws InvalidOrderException {
        logger.info("Updating order status: " + orderId + " to " + newStatus);
        
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            logger.warning("Attempted to update non-existent order: " + orderId);
            throw new InvalidOrderException("Order not found: " + orderId);
        }
        
        order.setStatus(newStatus);
        orderDAO.update(order);
        logger.info("Order status updated: " + orderId);
    }
    
    private void updateProductStock(String productId, int quantityChange) 
            throws ProductNotFoundException, InsufficientStockException {
        Product product = productDAO.findById(productId);
        
        if (product == null) {
            logger.warning("Product not found: " + productId);
            throw new ProductNotFoundException("Product not found: " + productId);
        }
        
        int newStock = product.getStock() + quantityChange;
        
        if (newStock < 0) {
            logger.warning("Insufficient stock for product: " + productId);
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getName() + 
                    ". Available: " + product.getStock());
        }
        
        product.setStock(newStock);
        productDAO.update(product);
        logger.info("Updated stock for product " + productId + ": " + newStock);
    }
}