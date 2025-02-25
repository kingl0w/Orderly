package main.java.model;

/**
 * orderitem class represents individual items within an order.
 * links products to orders with quantity information.
 * 
 * future enhancements:
 * - add price at time of order
 * - add item-specific notes
 */

public class OrderItem {
    private final Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    //getters and setters
    public Product getProduct() {
        return product; 
    }

    public int getQuantity() {
        return quantity; 
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity; 
    }

}
