package main.java.service;

import main.java.dao.CustomerDAO;
import main.java.model.Customer;
import main.java.util.LoggerUtil;

import java.util.List;

/**
 * customerservice handles business logic for customer operations.
 * 
 * future enhancements:
 * - add customer validation
 * - implement customer analytics
 * - add customer segmentation
 */
public class CustomerService {
    private final CustomerDAO customerDAO;
    private final LoggerUtil logger;
    
    public CustomerService() {
        this.customerDAO = CustomerDAO.getInstance();
        this.logger = LoggerUtil.getInstance();
        logger.info("CustomerService initialized");
    }
    
    public void createCustomer(Customer customer) {
        logger.info("Creating customer: " + customer.getId());
        
        //basic validation
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            logger.warning("Attempted to create customer with no name: " + customer.getId());
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            logger.warning("Attempted to create customer with no email: " + customer.getId());
            throw new IllegalArgumentException("Customer email cannot be empty");
        }
        
        customerDAO.save(customer);
        logger.info("Customer created successfully: " + customer.getId());
    }
    
    public Customer getCustomer(String id) {
        logger.info("Retrieving customer: " + id);
        Customer customer = customerDAO.findById(id);
        
        if (customer == null) {
            logger.warning("Customer not found: " + id);
        }
        
        return customer;
    }
    
    public List<Customer> getAllCustomers() {
        logger.info("Retrieving all customers");
        return customerDAO.findAll();
    }
    
    public List<Customer> searchCustomersByName(String name) {
        logger.info("Searching customers by name: " + name);
        return customerDAO.findByName(name);
    }
    
    public void updateCustomer(Customer customer) {
        logger.info("Updating customer: " + customer.getId());
        
        //checks if customer exists
        Customer existingCustomer = customerDAO.findById(customer.getId());
        if (existingCustomer == null) {
            logger.warning("Attempted to update non-existent customer: " + customer.getId());
            throw new IllegalArgumentException("Customer not found: " + customer.getId());
        }
        
        customerDAO.update(customer);
        logger.info("Customer updated successfully: " + customer.getId());
    }
    
    public void deleteCustomer(String id) {
        logger.info("Deleting customer: " + id);
        customerDAO.delete(id);
    }
}
