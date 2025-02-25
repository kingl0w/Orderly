package main.java.model;

/**
 * customer class represents client information in the system.
 * stores basic customer details needed for order processing.
 * 
 * future enhancements:
 * - add address information
 * - add order history
 */

public class Customer {
    private final String id;
    private String name;
    private String email;

    public Customer(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    //getters and setters
    public String getId(){
         return id;
    }

    public String getName(){ 
        return name; 
    }

    public void setName(String name) {
        this.name = name; 
    }

    public String getEmail() {
        return email; 
    }

    public void setEmail(String email) {
        this.email = email; 
    }
}