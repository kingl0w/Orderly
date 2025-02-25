package main.java.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * order class manages the ordering process and maintains
 * relationships between customers and products.
 * 
 * future enhancements:
 * - implement order status tracking
 * - add shipping information
 */

public class Order {
    private final String id;
    private final Customer customer;
    private final LocalDateTime orderDate;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(String id, Customer customer) {
        this.id = id;
        this.customer = customer;
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.status = OrderStatus.NEW;
    }

    public void addItem(Product product, int quantity) {
        items.add(new OrderItem(product, quantity));
    }

    //getters
    public String getId() {
        return id; 
    }

    public Customer getCustomer() {
        return customer; 
    }

    public LocalDateTime getOrderDate() {
        return orderDate; 
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items); 
    }

    public OrderStatus getStatus() {
        return status; 
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status; 
    }
}
