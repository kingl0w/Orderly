package main.java.dao;

import main.java.model.Customer;
import main.java.util.LoggerUtil;

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
    private final DatabaseProxy db;
    private final LoggerUtil logger;
    
    private CustomerDAO() {
        db = DatabaseProxy.getInstance();
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
        logger.info("Saving customer: " + customer.getId());
        db.save("customer:" + customer.getId(), customer);
    }
    
    public Customer findById(String id) {
        logger.info("Finding customer by ID: " + id);
        return (Customer) db.get("customer:" + id);
    }
    
    public List<Customer> findAll() {
        logger.info("Retrieving all customers");
        List<Customer> customers = new ArrayList<>();
        
        //for now, we'll just return an empty list as we don't have a way
        //to query all customers from our simple database proxy
        return customers;
    }
    
    public List<Customer> findByName(String name) {
        logger.info("Finding customers by name: " + name);
        String searchName = name.toLowerCase();
        List<Customer> customers = findAll();
        List<Customer> result = new ArrayList<>();
        
        for (Customer customer : customers) {
            if (customer.getName().toLowerCase().contains(searchName)) {
                result.add(customer);
            }
        }
        
        return result;
    }
    
    public void update(Customer customer) {
        logger.info("Updating customer: " + customer.getId());
        db.save("customer:" + customer.getId(), customer);
    }
    
    public void delete(String id) {
        logger.info("Deleting customer: " + id);
        db.delete("customer:" + id);
    }
}
