package main.java.model;

/**
 * orderstatus enum defines possible states for an order.
 * used to track order progress through the system.
 * 
 * future enhancements:
 * - add more detailed status options
 * - include status transition rules
 */

public enum OrderStatus {
    NEW,
    PROCESSING,
    COMPLETED,
    CANCELLED
}
