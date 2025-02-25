package main.java.ui;

import main.java.exception.InvalidOrderException;
import main.java.exception.ProductNotFoundException;
import main.java.model.*;
import main.java.service.CustomerService;
import main.java.service.OrderService;
import main.java.service.ProductService;
import main.java.util.LoggerUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * consoleui manages user interaction through command line interface.
 * handles input/output and menu navigation.
 * 
 * future enhancements:
 * - add input validation
 * - implement user authentication
 * - add more detailed menus
 */
public class ConsoleUI {
    private final Scanner scanner;
    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final LoggerUtil logger;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.productService = new ProductService();
        this.customerService = new CustomerService();
        this.orderService = new OrderService();
        this.logger = LoggerUtil.getInstance();
        logger.info("ConsoleUI initialized");
    }

    public void start() {
        logger.info("Application started");
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            logger.info("User selected menu option: " + choice);
            
            try {
                switch (choice) {
                    case 1:
                        productMenu();
                        break;
                    case 2:
                        customerMenu();
                        break;
                    case 3:
                        orderMenu();
                        break;
                    case 4:
                        logger.info("User chose to exit application");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        logger.warning("Invalid menu choice: " + choice);
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                logger.exception("Unhandled exception in menu", e);
            }
        }
        scanner.close();
        logger.info("Application terminated");
    }

    private void displayMainMenu() {
        System.out.println("\n===== Orderly Management System =====");
        System.out.println("1. Product Management");
        System.out.println("2. Customer Management");
        System.out.println("3. Order Management");
        System.out.println("4. Exit");
        System.out.println("=====================================");
    }

    // Product management
    
    private void productMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== Product Management =====");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Search Products");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Back to Main Menu");
            System.out.println("=============================");
            
            int choice = getIntInput("Enter your choice: ");
            logger.info("User selected product menu option: " + choice);
            
            try {
                switch (choice) {
                    case 1:
                        addProduct();
                        break;
                    case 2:
                        viewProducts();
                        break;
                    case 3:
                        searchProducts();
                        break;
                    case 4:
                        updateProduct();
                        break;
                    case 5:
                        deleteProduct();
                        break;
                    case 6:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        logger.warning("Invalid product menu choice: " + choice);
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                logger.exception("Unhandled exception in product menu", e);
            }
        }
    }

    private void addProduct() {
        System.out.println("\n===== Add New Product =====");
        String id = getStringInput("Enter product ID (or leave blank for auto-generated): ");
        if (id.trim().isEmpty()) {
            id = "P" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        String name = getStringInput("Enter product name: ");
        double price = getDoubleInput("Enter product price: ");
        String description = getStringInput("Enter product description: ");
        int stock = getIntInput("Enter initial stock: ");

        logger.info("Creating product: ID=" + id + ", Name=" + name);
        
        try {
            Product product = new Product.Builder()
                    .id(id)
                    .name(name)
                    .price(price)
                    .description(description)
                    .stock(stock)
                    .build();
    
            productService.createProduct(product);
            System.out.println("Product added successfully!");
            logger.info("Product added successfully: " + id);
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            logger.exception("Error while adding product", e);
        }
    }

    private void viewProducts() {
        System.out.println("\n===== Product Catalog =====");
        logger.info("User requested to view all products");
        
        try {
            List<Product> products = productService.getAllProducts();
            if (products.isEmpty()) {
                System.out.println("No products found.");
                logger.info("Product catalog is empty");
                return;
            }
    
            displayProductTable(products);
            logger.info("Displayed " + products.size() + " products");
        } catch (Exception e) {
            System.out.println("Error retrieving products: " + e.getMessage());
            logger.exception("Error while retrieving products", e);
        }
    }
    
    private void displayProductTable(List<Product> products) {
        System.out.println("\n------------------------------------------------");
        System.out.printf("%-10s %-20s %-10s %-10s\n", "ID", "Name", "Price", "Stock");
        System.out.println("------------------------------------------------");
        
        for (Product product : products) {
            System.out.printf("%-10s %-20s $%-9.2f %-10d\n", 
                    product.getId(), 
                    truncate(product.getName(), 20), 
                    product.getPrice(), 
                    product.getStock());
        }
        System.out.println("------------------------------------------------");
    }
    
    private String truncate(String text, int length) {
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length - 3) + "...";
    }

    private void searchProducts() {
        System.out.println("\n===== Search Products =====");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Price Range");
        System.out.println("3. Back");
        
        int choice = getIntInput("Enter your choice: ");
        logger.info("User selected product search option: " + choice);
        
        try {
            switch (choice) {
                case 1:
                    searchProductsByName();
                    break;
                case 2:
                    searchProductsByPriceRange();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    logger.warning("Invalid product search choice: " + choice);
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            logger.exception("Error in product search", e);
        }
    }
    
    private void searchProductsByName() {
        String name = getStringInput("Enter product name to search: ");
        logger.info("Searching products by name: " + name);
        
        List<Product> products = productService.searchProductsByName(name);
        if (products.isEmpty()) {
            System.out.println("No products found matching: " + name);
            return;
        }
        
        System.out.println("\nFound " + products.size() + " matching products:");
        displayProductTable(products);
    }
    
    private void searchProductsByPriceRange() {
        double minPrice = getDoubleInput("Enter minimum price: ");
        double maxPrice = getDoubleInput("Enter maximum price: ");
        logger.info("Searching products by price range: " + minPrice + " - " + maxPrice);
        
        List<Product> products = productService.searchProductsByPriceRange(minPrice, maxPrice);
        if (products.isEmpty()) {
            System.out.println("No products found in the price range: $" + minPrice + " - $" + maxPrice);
            return;
        }
        
        System.out.println("\nFound " + products.size() + " products in the price range:");
        displayProductTable(products);
    }
    
    private void updateProduct() {
        String id = getStringInput("Enter product ID to update: ");
        logger.info("Attempting to update product: " + id);
        
        try {
            Product product = productService.getProduct(id);
            System.out.println("Current product details:");
            System.out.println("  Name: " + product.getName());
            System.out.println("  Price: $" + product.getPrice());
            System.out.println("  Description: " + product.getDescription());
            System.out.println("  Stock: " + product.getStock());
            
            System.out.println("\nEnter new details (leave blank to keep current value):");
            
            String name = getStringInput("Name: ");
            if (!name.trim().isEmpty()) {
                product.setName(name);
            }
            
            String priceStr = getStringInput("Price: ");
            if (!priceStr.trim().isEmpty()) {
                product.setPrice(Double.parseDouble(priceStr));
            }
            
            String description = getStringInput("Description: ");
            if (!description.trim().isEmpty()) {
                product.setDescription(description);
            }
            
            String stockStr = getStringInput("Stock: ");
            if (!stockStr.trim().isEmpty()) {
                product.setStock(Integer.parseInt(stockStr));
            }
            
            productService.updateProduct(product);
            System.out.println("Product updated successfully!");
            logger.info("Product updated: " + id);
            
        } catch (ProductNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warning("Product not found for update: " + id);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format");
            logger.warning("Invalid number format during product update");
        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
            logger.exception("Error updating product", e);
        }
    }
    
    private void deleteProduct() {
        String id = getStringInput("Enter product ID to delete: ");
        logger.info("Attempting to delete product: " + id);
        
        try {
            String confirm = getStringInput("Are you sure you want to delete this product? (y/n): ");
            if (confirm.toLowerCase().startsWith("y")) {
                productService.deleteProduct(id);
                System.out.println("Product deleted successfully!");
                logger.info("Product deleted: " + id);
            } else {
                System.out.println("Delete operation cancelled.");
                logger.info("Product deletion cancelled for: " + id);
            }
        } catch (ProductNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warning("Product not found for deletion: " + id);
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
            logger.exception("Error deleting product", e);
        }
    }
    
    //customer management
    
    private void customerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== Customer Management =====");
            System.out.println("1. Add Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. Search Customers");
            System.out.println("4. Back to Main Menu");
            System.out.println("===============================");
            
            int choice = getIntInput("Enter your choice: ");
            logger.info("User selected customer menu option: " + choice);
            
            try {
                switch (choice) {
                    case 1:
                        addCustomer();
                        break;
                    case 2:
                        viewCustomers();
                        break;
                    case 3:
                        searchCustomers();
                        break;
                    case 4:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        logger.warning("Invalid customer menu choice: " + choice);
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                logger.exception("Unhandled exception in customer menu", e);
            }
        }
    }
    
    private void addCustomer() {
        System.out.println("\n===== Add New Customer =====");
        String id = getStringInput("Enter customer ID (or leave blank for auto-generated): ");
        if (id.trim().isEmpty()) {
            id = "C" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        String name = getStringInput("Enter customer name: ");
        String email = getStringInput("Enter customer email: ");
        
        logger.info("Creating customer: ID=" + id + ", Name=" + name);
        
        try {
            Customer customer = new Customer(id, name, email);
            customerService.createCustomer(customer);
            System.out.println("Customer added successfully!");
            logger.info("Customer added successfully: " + id);
        } catch (Exception e) {
            System.out.println("Error adding customer: " + e.getMessage());
            logger.exception("Error while adding customer", e);
        }
    }
    
    private void viewCustomers() {
        System.out.println("\n===== Customer List =====");
        logger.info("User requested to view all customers");
        
        try {
            List<Customer> customers = customerService.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers found.");
                logger.info("Customer list is empty");
                return;
            }
            
            displayCustomerTable(customers);
            logger.info("Displayed " + customers.size() + " customers");
        } catch (Exception e) {
            System.out.println("Error retrieving customers: " + e.getMessage());
            logger.exception("Error while retrieving customers", e);
        }
    }
    
    private void displayCustomerTable(List<Customer> customers) {
        System.out.println("\n---------------------------------------------");
        System.out.printf("%-10s %-20s %-20s\n", "ID", "Name", "Email");
        System.out.println("---------------------------------------------");
        
        for (Customer customer : customers) {
            System.out.printf("%-10s %-20s %-20s\n", 
                    customer.getId(), 
                    truncate(customer.getName(), 20), 
                    truncate(customer.getEmail(), 20));
        }
        System.out.println("---------------------------------------------");
    }
    
    private void searchCustomers() {
        String name = getStringInput("Enter customer name to search: ");
        logger.info("Searching customers by name: " + name);
        
        List<Customer> customers = customerService.searchCustomersByName(name);
        if (customers.isEmpty()) {
            System.out.println("No customers found matching: " + name);
            return;
        }
        
        System.out.println("\nFound " + customers.size() + " matching customers:");
        displayCustomerTable(customers);
    }
    
    //order management
    
    private void orderMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== Order Management =====");
            System.out.println("1. Create New Order");
            System.out.println("2. View All Orders");
            System.out.println("3. Search Orders");
            System.out.println("4. Update Order Status");
            System.out.println("5. Back to Main Menu");
            System.out.println("============================");
            
            int choice = getIntInput("Enter your choice: ");
            logger.info("User selected order menu option: " + choice);
            
            try {
                switch (choice) {
                    case 1:
                        createOrder();
                        break;
                    case 2:
                        viewOrders();
                        break;
                    case 3:
                        searchOrders();
                        break;
                    case 4:
                        updateOrderStatus();
                        break;
                    case 5:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        logger.warning("Invalid order menu choice: " + choice);
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                logger.exception("Unhandled exception in order menu", e);
            }
        }
    }
    
    private void createOrder() {
        System.out.println("\n===== Create New Order =====");
        
        //generate order ID
        String orderId = "O" + UUID.randomUUID().toString().substring(0, 8);
        
        //get customer
        String customerId = getStringInput("Enter customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        
        if (customer == null) {
            System.out.println("Customer not found. Please create a customer first.");
            logger.warning("Attempted to create order with non-existent customer: " + customerId);
            return;
        }
        
        //create order
        Order order = new Order(orderId, customer);
        
        //add items to order
        boolean addingItems = true;
        while (addingItems) {
            try {
                //get product details
                String productId = getStringInput("Enter product ID: ");
                Product product = productService.getProduct(productId);
                
                System.out.println("Selected product: " + product.getName() + " - $" + product.getPrice());
                System.out.println("Available stock: " + product.getStock());
                
                int quantity = getIntInput("Enter quantity: ");
                if (quantity <= 0) {
                    System.out.println("Quantity must be greater than zero.");
                    continue;
                }
                
                if (quantity > product.getStock()) {
                    System.out.println("Error: Insufficient stock. Available: " + product.getStock());
                    continue;
                }
                
                //add item to order
                order.addItem(product, quantity);
                System.out.println("Item added to order.");
                
                //ask if user wants to add more items
                String more = getStringInput("Add another item? (y/n): ");
                if (!more.toLowerCase().startsWith("y")) {
                    addingItems = false;
                }
                
            } catch (ProductNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
                logger.warning("Product not found during order creation");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.exception("Error adding item to order", e);
            }
        }
        
        //finalize order
        if (order.getItems().isEmpty()) {
            System.out.println("Order cancelled: No items added.");
            logger.info("Order creation cancelled - no items added");
            return;
        }
        
        try {
            orderService.createOrder(order);
            System.out.println("\nOrder created successfully!");
            System.out.println("Order ID: " + order.getId());
            logger.info("Order created successfully: " + order.getId());
        } catch (InvalidOrderException e) {
            System.out.println("Error creating order: " + e.getMessage());
            logger.warning("Invalid order: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
            logger.exception("Error creating order", e);
        }
    }
    
    private void viewOrders() {
        System.out.println("\n===== Order List =====");
        logger.info("User requested to view all orders");
        
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders.isEmpty()) {
                System.out.println("No orders found.");
                logger.info("Order list is empty");
                return;
            }
            
            displayOrderTable(orders);
            logger.info("Displayed " + orders.size() + " orders");
        } catch (Exception e) {
            System.out.println("Error retrieving orders: " + e.getMessage());
            logger.exception("Error while retrieving orders", e);
        }
    }
    
    private void displayOrderTable(List<Order> orders) {
        System.out.println("\n---------------------------------------------------");
        System.out.printf("%-10s %-15s %-15s %-15s\n", "Order ID", "Customer", "Date", "Status");
        System.out.println("---------------------------------------------------");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Order order : orders) {
            System.out.printf("%-10s %-15s %-15s %-15s\n", 
                    order.getId(), 
                    truncate(order.getCustomer().getName(), 15), 
                    order.getOrderDate().format(formatter),
                    order.getStatus());
        }
        System.out.println("---------------------------------------------------");
    }
    
    private void searchOrders() {
        System.out.println("\n===== Search Orders =====");
        System.out.println("1. Search by Customer ID");
        System.out.println("2. Search by Date Range");
        System.out.println("3. Back");
        
        int choice = getIntInput("Enter your choice: ");
        logger.info("User selected order search option: " + choice);
        
        try {
            switch (choice) {
                case 1:
                    searchOrdersByCustomer();
                    break;
                case 2:
                    searchOrdersByDateRange();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    logger.warning("Invalid order search choice: " + choice);
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            logger.exception("Error in order search", e);
        }
    }
    
    private void searchOrdersByCustomer() {
        String customerId = getStringInput("Enter customer ID: ");
        logger.info("Searching orders by customer ID: " + customerId);
        
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        if (orders.isEmpty()) {
            System.out.println("No orders found for customer: " + customerId);
            return;
        }
        
        System.out.println("\nFound " + orders.size() + " orders for customer:");
        displayOrderTable(orders);
    }
    
    private void searchOrdersByDateRange() {
        System.out.println("Enter start date (yyyy-MM-dd):");
        LocalDateTime startDate = getDateInput();
        System.out.println("Enter end date (yyyy-MM-dd):");
        LocalDateTime endDate = getDateInput().plusDays(1);  //add a day to include the end date
        
        logger.info("Searching orders between " + startDate + " and " + endDate);
        
        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
        if (orders.isEmpty()) {
            System.out.println("No orders found in the selected date range.");
            return;
        }
        
        System.out.println("\nFound " + orders.size() + " orders in the date range:");
        displayOrderTable(orders);
    }
    
    private LocalDateTime getDateInput() {
        while (true) {
            try {
                String dateStr = scanner.nextLine();
                return LocalDate.parse(dateStr).atStartOfDay();
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd format:");
                logger.warning("Invalid date format entered");
            }
        }
    }
    
    private void updateOrderStatus() {
        String orderId = getStringInput("Enter order ID: ");
        logger.info("Updating status for order: " + orderId);
        
        try {
            Order order = orderService.getOrder(orderId);
            if (order == null) {
                System.out.println("Order not found: " + orderId);
                logger.warning("Order not found for status update: " + orderId);
                return;
            }
            
            System.out.println("Current status: " + order.getStatus());
            System.out.println("\nSelect new status:");
            System.out.println("1. NEW");
            System.out.println("2. PROCESSING");
            System.out.println("3. COMPLETED");
            System.out.println("4. CANCELLED");
            
            int choice = getIntInput("Enter your choice: ");
            OrderStatus newStatus;
            
            switch (choice) {
                case 1:
                    newStatus = OrderStatus.NEW;
                    break;
                case 2:
                    newStatus = OrderStatus.PROCESSING;
                    break;
                case 3:
                    newStatus = OrderStatus.COMPLETED;
                    break;
                case 4:
                    newStatus = OrderStatus.CANCELLED;
                    break;
                default:
                    System.out.println("Invalid choice. Status update cancelled.");
                    logger.warning("Invalid status choice: " + choice);
                    return;
            }
            
            orderService.updateOrderStatus(orderId, newStatus);
            System.out.println("Order status updated successfully!");
            logger.info("Order status updated for " + orderId + ": " + newStatus);
            
        } catch (InvalidOrderException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warning("Invalid order for status update: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating order status: " + e.getMessage());
            logger.exception("Error updating order status", e);
        }
    }
    
    //utility methods
    
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                logger.warning("Invalid number input from user");
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                logger.warning("Invalid decimal input from user");
            }
        }
    }
}