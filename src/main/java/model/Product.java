package main.java.model;

/**
 * product class represents items in the order management system.
 * this class implements both Builder and Prototype patterns to facilitate
 * flexible object creation and cloning capabilities.
 * 
 * future enhancements:
 * - add category field for product organization
 * - implement inventory tracking
 */

//using Prototype pattern
public class Product implements Cloneable {
    private String id;
    private String name;
    private double price;
    private String description;
    private int stock;

    private Product() {} //private constructor for Builder

    @Override
    public Product clone() {
        try {
            return (Product) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //builder pattern
    public static class Builder {
        private final Product product;

        public Builder() {
            product = new Product();
        }

        public Builder id(String id) {
            product.id = id;
            return this;
        }

        public Builder name(String name) {
            product.name = name;
            return this;
        }

        public Builder price(double price) {
            product.price = price;
            return this;
        }

        public Builder description(String description) {
            product.description = description;
            return this;
        }

        public Builder stock(int stock) {
            product.stock = stock;
            return this;
        }

        public Product build() {
            return product;
        }
    }

    //getters and setters
    public String getId() { 
        return id; 
    }

    public String getName() {
        return name; 
    }

    public void setName(String name) {
        this.name = name; 
    }

    public double getPrice() {
        return price; 
    }

    public void setPrice(double price) {
        this.price = price; 
    }

    public String getDescription() {
        return description; 
    }

    public void setDescription(String description) {
        this.description = description; 
    }

    public int getStock() {
        return stock; 
    }

    public void setStock(int stock) {
        this.stock = stock; 
    }
}
