package main.java.ui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import main.java.service.CustomerService;
import main.java.service.OrderService;
import main.java.service.ProductService;
import main.java.model.Customer;
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.OrderStatus;
import main.java.model.Product;
import main.java.util.LoggerUtil;

public class SwingUI extends JFrame {
    private final ProductService productService;
    private final CustomerService customerService;
    //store OrderService as a field so we can call it everywhere
    private final OrderService orderService;

    private final LoggerUtil logger;
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public SwingUI() {
        this.productService = new ProductService();
        this.customerService = new CustomerService();
        this.orderService = new OrderService();   
        this.logger = LoggerUtil.getInstance();
        
        setTitle("Orderly Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        //main menu
        JPanel menuPanel = createMainMenuPanel();
        mainPanel.add(menuPanel, "MainMenu");
        
        //products
        JPanel productsPanel = createProductsPanel();
        mainPanel.add(productsPanel, "Products");
        
        //customers
        JPanel customersPanel = createCustomersPanel();
        mainPanel.add(customersPanel, "Customers");
        
        //orders
        JPanel ordersPanel = createOrdersPanel();
        mainPanel.add(ordersPanel, "Orders");
        
        //reports
        JPanel reportsPanel = createReportsPanel();
        mainPanel.add(reportsPanel, "Reports");
        
        //show main menu by default
        cardLayout.show(mainPanel, "MainMenu");
        
        getContentPane().add(mainPanel);
        
        logger.info("GUI initialized");
    }
    
    //simple main menu instance
    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JButton productsButton = new JButton("Manage Products");
        JButton customersButton = new JButton("Manage Customers");
        JButton ordersButton = new JButton("Manage Orders");
        JButton reportsButton = new JButton("View Reports");
        JButton exitButton = new JButton("Exit");
        
        productsButton.addActionListener(e -> cardLayout.show(mainPanel, "Products"));
        customersButton.addActionListener(e -> cardLayout.show(mainPanel, "Customers"));
        ordersButton.addActionListener(e -> cardLayout.show(mainPanel, "Orders"));
        reportsButton.addActionListener(e -> cardLayout.show(mainPanel, "Reports"));
        exitButton.addActionListener(e -> System.exit(0));
        
        panel.add(productsButton);
        panel.add(customersButton);
        panel.add(ordersButton);
        panel.add(reportsButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    //product management table
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Product Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        //create table with selection model
        String[] columns = {"ID", "Name", "Price", "Stock", "Description"};
        DefaultTableModel tableModel = new DefaultTableModel(new Object[0][5], columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; //make table read-only
            }
        };
        
        JTable productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.getTableHeader().setReorderingAllowed(false);
        
        //add table sorting capability
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        productsTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(productsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        //search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchProducts(searchField.getText(), sorter); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchProducts(searchField.getText(), sorter); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchProducts(searchField.getText(), sorter); }
        });
        
        //button panel
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton backButton = new JButton("Back to Main Menu");
        
        //initially disable edit/delete until a row is selected
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        
        //add action listeners
        refreshButton.addActionListener(__ -> refreshProductsTable(productsTable));
        addButton.addActionListener(__ -> showAddProductDialog(productsTable));
        editButton.addActionListener(__ -> {
            int row = productsTable.getSelectedRow();
            if (row >= 0) {
                row = productsTable.convertRowIndexToModel(row); 
                String productId = (String) tableModel.getValueAt(row, 0);
                showEditProductDialog(productId, productsTable);
            }
        });
        deleteButton.addActionListener(__ -> {
            int row = productsTable.getSelectedRow();
            if (row >= 0) {
                row = productsTable.convertRowIndexToModel(row); 
                String productId = (String) tableModel.getValueAt(row, 0);
                deleteProduct(productId, productsTable);
            }
        });
        backButton.addActionListener(__ -> cardLayout.show(mainPanel, "MainMenu"));
        
        //enable/disable edit/delete buttons based on selection
        productsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = productsTable.getSelectedRow() >= 0;
            editButton.setEnabled(rowSelected);
            deleteButton.setEnabled(rowSelected);
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(searchPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        //load products initially
        refreshProductsTable(productsTable);
        
        return panel;
    }
    
    //helper method for product search
    private void searchProducts(String text, TableRowSorter<TableModel> sorter) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            //case-insensitive search across all columns
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
    
    //updated table refresh method
    private void refreshProductsTable(JTable table) {
        List<Product> products = productService.getAllProducts();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        //clear existing data
        model.setRowCount(0);
        
        //add new data
        for (Product p : products) {
            model.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getStock(),
                p.getDescription()
            });
        }
        
        //inform user if no products
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No products found in the database. Add some products to get started.",
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    //modified add product method with better validation
    private void showAddProductDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("ID:"));
        JTextField idField = new JTextField();
        panel.add(idField);
        
        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField();
        panel.add(priceField);
        
        panel.add(new JLabel("Stock:"));
        JTextField stockField = new JTextField();
        panel.add(stockField);
        
        panel.add(new JLabel("Description:"));
        JTextField descField = new JTextField();
        panel.add(descField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(__ -> {
            try {
                //validate required fields
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Product name is required", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                //validate price and stock
                double price;
                int stock;
                
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                    if (price < 0) {
                        throw new NumberFormatException("Price cannot be negative");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Please enter a valid price (positive number)", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    stock = Integer.parseInt(stockField.getText().trim());
                    if (stock < 0) {
                        throw new NumberFormatException("Stock cannot be negative");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Please enter a valid stock quantity (positive integer)", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                //create product with validated values
                String id = idField.getText().trim();
                if (id.isEmpty()) {
                    id = "P" + UUID.randomUUID().toString().substring(0, 8);
                }
                
                Product product = new Product.Builder()
                        .id(id)
                        .name(nameField.getText().trim())
                        .price(price)
                        .stock(stock)
                        .description(descField.getText().trim())
                        .build();
                
                productService.createProduct(product);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                refreshProductsTable(table);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                        "Error adding product: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                logger.exception("Error adding product", ex);
            }
        });
        
        cancelButton.addActionListener(__ -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    //new method for editing a product
    private void showEditProductDialog(String productId, JTable table) {
        try {
            Product product = productService.getProduct(productId);
            
            if (product == null) {
                JOptionPane.showMessageDialog(this, 
                        "Product not found", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JDialog dialog = new JDialog(this, "Edit Product", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            panel.add(new JLabel("ID:"));
            JTextField idField = new JTextField(product.getId());
            idField.setEditable(false); // Can't change ID
            panel.add(idField);
            
            panel.add(new JLabel("Name:"));
            JTextField nameField = new JTextField(product.getName());
            panel.add(nameField);
            
            panel.add(new JLabel("Price:"));
            JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
            panel.add(priceField);
            
            panel.add(new JLabel("Stock:"));
            JTextField stockField = new JTextField(String.valueOf(product.getStock()));
            panel.add(stockField);
            
            panel.add(new JLabel("Description:"));
            JTextField descField = new JTextField(product.getDescription());
            panel.add(descField);
            
            JButton saveButton = new JButton("Update");
            JButton cancelButton = new JButton("Cancel");
            
            saveButton.addActionListener(__ -> {
                try {
                    //validation (same as add product)
                    if (nameField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, 
                                "Product name is required", 
                                "Validation Error", 
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    double price;
                    int stock;
                    
                    try {
                        price = Double.parseDouble(priceField.getText().trim());
                        if (price < 0) {
                            throw new NumberFormatException("Price cannot be negative");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, 
                                "Please enter a valid price (positive number)", 
                                "Validation Error", 
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    try {
                        stock = Integer.parseInt(stockField.getText().trim());
                        if (stock < 0) {
                            throw new NumberFormatException("Stock cannot be negative");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, 
                                "Please enter a valid stock quantity (positive integer)", 
                                "Validation Error", 
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //update product with validated values
                    product.setName(nameField.getText().trim());
                    product.setPrice(price);
                    product.setStock(stock);
                    product.setDescription(descField.getText().trim());
                    
                    productService.updateProduct(product);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                    refreshProductsTable(table);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Error updating product: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    logger.exception("Error updating product", ex);
                }
            });
            
            cancelButton.addActionListener(__ -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            dialog.setLayout(new BorderLayout());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error loading product: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            logger.exception("Error loading product for editing", ex);
        }
    }
    
    //new method for deleting a product
    private void deleteProduct(String productId, JTable table) {
        try {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this product?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                productService.deleteProduct(productId);
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                refreshProductsTable(table);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error deleting product: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            logger.exception("Error deleting product", ex);
        }
    }

    //customer management panel
    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Customer Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        //create table with selection model
        String[] columns = {"ID", "Name", "Email"};
        DefaultTableModel tableModel = new DefaultTableModel(new Object[0][3], columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        JTable customersTable = new JTable(tableModel);
        customersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customersTable.getTableHeader().setReorderingAllowed(false);
        
        //add table sorting capability
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        customersTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(customersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        //search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchCustomers(searchField.getText(), sorter); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchCustomers(searchField.getText(), sorter); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchCustomers(searchField.getText(), sorter); }
        });
        
        //button panel
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Customer");
        JButton editButton = new JButton("Edit Customer");
        JButton deleteButton = new JButton("Delete Customer");
        JButton ordersButton = new JButton("View Orders");
        JButton backButton = new JButton("Back to Main Menu");
        
        //initially disable edit/delete until a row is selected
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        ordersButton.setEnabled(false);
        
        //add action listeners
        refreshButton.addActionListener(__ -> refreshCustomersTable(customersTable));
        addButton.addActionListener(__ -> showAddCustomerDialog(customersTable));
        editButton.addActionListener(__ -> {
            int row = customersTable.getSelectedRow();
            if (row >= 0) {
                row = customersTable.convertRowIndexToModel(row); 
                String customerId = (String) tableModel.getValueAt(row, 0);
                showEditCustomerDialog(customerId, customersTable);
            }
        });
        deleteButton.addActionListener(__ -> {
            int row = customersTable.getSelectedRow();
            if (row >= 0) {
                row = customersTable.convertRowIndexToModel(row);
                String customerId = (String) tableModel.getValueAt(row, 0);
                deleteCustomer(customerId, customersTable);
            }
        });
        ordersButton.addActionListener(__ -> {
            int row = customersTable.getSelectedRow();
            if (row >= 0) {
                row = customersTable.convertRowIndexToModel(row);
                String customerId = (String) tableModel.getValueAt(row, 0);
                showCustomerOrders(customerId);
            }
        });
        backButton.addActionListener(__ -> cardLayout.show(mainPanel, "MainMenu"));
        
        //enable/disable buttons based on selection
        customersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = customersTable.getSelectedRow() >= 0;
            editButton.setEnabled(rowSelected);
            deleteButton.setEnabled(rowSelected);
            ordersButton.setEnabled(rowSelected);
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(ordersButton);
        buttonPanel.add(backButton);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(searchPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        //load customers initially
        refreshCustomersTable(customersTable);
        
        return panel;
    }
    
    //helper method for customer search
    private void searchCustomers(String text, TableRowSorter<TableModel> sorter) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            //case-insensitive search across all columns
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
    
    private void refreshCustomersTable(JTable table) {
        List<Customer> customers = customerService.getAllCustomers();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        //clear existing data
        model.setRowCount(0);
        
        //add new data
        for (Customer c : customers) {
            model.addRow(new Object[]{
                c.getId(),
                c.getName(),
                c.getEmail()
            });
        }
        
        //inform user if no customers
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No customers found in the database. Add some customers to get started.",
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    //modified add customer method with better validation
    private void showAddCustomerDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Add Customer", true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("ID:"));
        JTextField idField = new JTextField();
        panel.add(idField);
        
        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(__ -> {
            try {
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Customer name is required", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (emailField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Email is required", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                //validate email format
                String email = emailField.getText().trim();
                if (!email.contains("@") || !email.contains(".")) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Please enter a valid email address", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                //create customer with validated values
                String id = idField.getText().trim();
                if (id.isEmpty()) {
                    id = "C" + UUID.randomUUID().toString().substring(0, 8);
                }
                
                Customer customer = new Customer(id, nameField.getText().trim(), email);
                customerService.createCustomer(customer);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                refreshCustomersTable(table);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                        "Error adding customer: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                logger.exception("Error adding customer", ex);
            }
        });
        
        cancelButton.addActionListener(__ -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    //new method for editing a customer
    private void showEditCustomerDialog(String customerId, JTable table) {
        try {
            Customer customer = customerService.getCustomer(customerId);
            
            if (customer == null) {
                JOptionPane.showMessageDialog(this, 
                        "Customer not found", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JDialog dialog = new JDialog(this, "Edit Customer", true);
            dialog.setSize(400, 220);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            panel.add(new JLabel("ID:"));
            JTextField idField = new JTextField(customer.getId());
            idField.setEditable(false); 
            panel.add(idField);
            
            panel.add(new JLabel("Name:"));
            JTextField nameField = new JTextField(customer.getName());
            panel.add(nameField);
            
            panel.add(new JLabel("Email:"));
            JTextField emailField = new JTextField(customer.getEmail());
            panel.add(emailField);
            
            JButton saveButton = new JButton("Update");
            JButton cancelButton = new JButton("Cancel");
            
            saveButton.addActionListener(__ -> {
                try {
                    if (nameField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, 
                                "Customer name is required", 
                                "Validation Error", 
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (emailField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, 
                                "Email is required", 
                                "Validation Error", 
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //validate email format
                    String email = emailField.getText().trim();
                    if (!email.contains("@") || !email.contains(".")) {
                        JOptionPane.showMessageDialog(dialog, 
                                "Please enter a valid email address", 
                                "Validation Error", 
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //update customer with validated values
                    customer.setName(nameField.getText().trim());
                    customer.setEmail(email);
                    
                    customerService.updateCustomer(customer);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Customer updated successfully!");
                    refreshCustomersTable(table);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Error updating customer: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    logger.exception("Error updating customer", ex);
                }
            });
            
            cancelButton.addActionListener(__ -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            dialog.setLayout(new BorderLayout());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error loading customer: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            logger.exception("Error loading customer for editing", ex);
        }
    }
    
    //new method for deleting a customer
    private void deleteCustomer(String customerId, JTable table) {
        try {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this customer? This will also delete all associated orders.",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                customerService.deleteCustomer(customerId);
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                refreshCustomersTable(table);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error deleting customer: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            logger.exception("Error deleting customer", ex);
        }
    }
    
    //method to show orders for a specific customer
    private void showCustomerOrders(String customerId) {
        try {
            Customer customer = customerService.getCustomer(customerId);
            if (customer == null) {
                JOptionPane.showMessageDialog(this, 
                        "Customer not found", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            
            JDialog dialog = new JDialog(this, "Orders for " + customer.getName(), true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            
            //create orders table
            String[] columns = {"Order ID", "Date", "Status", "Items", "Total"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable ordersTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(ordersTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            //date formatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            if (orders.isEmpty()) {
                JLabel noOrdersLabel = new JLabel("No orders found for this customer", JLabel.CENTER);
                noOrdersLabel.setFont(new Font("Arial", Font.BOLD, 16));
                panel.add(noOrdersLabel, BorderLayout.CENTER);
            } else {
                for (Order order : orders) {
                    double total = 0;
                    for (OrderItem item : order.getItems()) {
                        total += item.getProduct().getPrice() * item.getQuantity();
                    }
                    
                    model.addRow(new Object[]{
                        order.getId(),
                        order.getOrderDate().format(formatter),
                        order.getStatus(),
                        order.getItems().size(),
                        String.format("$%.2f", total) 
                    });
                }
            }

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(__ -> dialog.dispose());
        
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
        
            panel.add(buttonPanel, BorderLayout.SOUTH);
            dialog.add(panel);
        
            dialog.setVisible(true);
        
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading customer orders: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            logger.exception("Error loading customer orders", ex);
        }
    }

    //reports panel
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Reports", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create report options panel
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        JButton inventoryReportButton = new JButton("Inventory Report");
        JButton salesReportButton = new JButton("Sales Report");
        JButton customerReportButton = new JButton("Customer Report");
        JButton backButton = new JButton("Back to Main Menu");
        
        inventoryReportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        salesReportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        customerReportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        inventoryReportButton.addActionListener(__ -> showInventoryReport());
        salesReportButton.addActionListener(__ -> showSalesReport());
        customerReportButton.addActionListener(__ -> showCustomerReport());
        backButton.addActionListener(__ -> cardLayout.show(mainPanel, "MainMenu"));
        
        optionsPanel.add(inventoryReportButton);
        optionsPanel.add(salesReportButton);
        optionsPanel.add(customerReportButton);
        optionsPanel.add(backButton);
        
        //add description
        JPanel descPanel = new JPanel(new BorderLayout());
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>"
                + "Generate reports to analyze your business data.<br>"
                + "Choose a report type from the options below.</div></html>", JLabel.CENTER);
        descPanel.add(descLabel, BorderLayout.CENTER);
        
        //combine panels
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(descPanel, BorderLayout.NORTH);
        contentPanel.add(optionsPanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    //inventory report
    private void showInventoryReport() {
        JDialog dialog = new JDialog(this, "Inventory Report", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        //report header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Inventory Status Report", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        //get current date for report
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        JLabel dateLabel = new JLabel("Generated on: " + now.format(formatter), JLabel.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        //report content
        try {
            List<Product> products = productService.getAllProducts();
            
            if (products.isEmpty()) {
                JLabel noDataLabel = new JLabel("No products found in inventory", JLabel.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.BOLD, 14));
                panel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                //calculate statistics
                double totalValue = 0;
                int totalItems = 0;
                int lowStockItems = 0;
                final int LOW_STOCK_THRESHOLD = 10;
                
                for (Product p : products) {
                    totalValue += p.getPrice() * p.getStock();
                    totalItems += p.getStock();
                    if (p.getStock() < LOW_STOCK_THRESHOLD) {
                        lowStockItems++;
                    }
                }
                
                //create report table
                String[] columns = {"ID", "Name", "Price", "Stock", "Value", "Status"};
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                
                JTable inventoryTable = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(inventoryTable);
                
                //sort products by stock level (ascending)
                products.sort(Comparator.comparing(Product::getStock));
                
                //add products to table
                for (Product p : products) {
                    double value = p.getPrice() * p.getStock();
                    String status = p.getStock() < LOW_STOCK_THRESHOLD ? "LOW STOCK" : "OK";
                    
                    model.addRow(new Object[]{
                            p.getId(),
                            p.getName(),
                            String.format("$%.2f", p.getPrice()),
                            p.getStock(),
                            String.format("$%.2f", value),
                            status
                    });
                }
                
                panel.add(scrollPane, BorderLayout.CENTER);
                
                //summary panel
                JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 0));
                summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                JLabel totalItemsLabel = new JLabel("Total Items: " + totalItems);
                JLabel lowStockLabel = new JLabel("Low Stock Items: " + lowStockItems);
                JLabel totalValueLabel = new JLabel("Total Value: $" + String.format("%.2f", totalValue));
                
                summaryPanel.add(totalItemsLabel);
                summaryPanel.add(lowStockLabel);
                summaryPanel.add(totalValueLabel);
                
                panel.add(summaryPanel, BorderLayout.SOUTH);
            }
        } catch (Exception ex) {
            JLabel errorLabel = new JLabel("Error generating report: " + ex.getMessage(), JLabel.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
            logger.exception("Error generating inventory report", ex);
        }
        
        //add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(__ -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    //sales Report
    private void showSalesReport() {
        JDialog dialog = new JDialog(this, "Sales Report", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        //report header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Sales Analysis Report", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        //get current date for report
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        JLabel dateLabel = new JLabel("Generated on: " + now.format(formatter), JLabel.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        //report content 
        JLabel infoLabel = new JLabel("<html><div style='text-align: center;'>"
                + "This is a placeholder for the Sales Report.<br>"
                + "In a production environment, this would show sales analytics<br>"
                + "based on order data from the database.</div></html>", JLabel.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(infoLabel, BorderLayout.CENTER);
        
        //add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(__ -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    //customer Report
    private void showCustomerReport() {
        JDialog dialog = new JDialog(this, "Customer Report", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        //report header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Customer Summary Report", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        //get current date for report
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        JLabel dateLabel = new JLabel("Generated on: " + now.format(formatter), JLabel.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        //report content
        try {
            List<Customer> customers = customerService.getAllCustomers();
            
            if (customers.isEmpty()) {
                JLabel noDataLabel = new JLabel("No customers found in database", JLabel.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.BOLD, 14));
                panel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                //create report table
                String[] columns = {"ID", "Name", "Email", "Orders"};
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                
                JTable customerTable = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(customerTable);
                
                //add customers to table
                for (Customer c : customers) {
                    List<Order> orders = orderService.getOrdersByCustomer(c.getId());
                    
                    model.addRow(new Object[]{
                            c.getId(),
                            c.getName(),
                            c.getEmail(),
                            orders.size()
                    });
                }
                
                panel.add(scrollPane, BorderLayout.CENTER);
                
                //summary panel
                JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 10, 0));
                summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                JLabel totalCustomersLabel = new JLabel("Total Customers: " + customers.size());
                summaryPanel.add(totalCustomersLabel);
                
                panel.add(summaryPanel, BorderLayout.SOUTH);
            }
        } catch (Exception ex) {
            JLabel errorLabel = new JLabel("Error generating report: " + ex.getMessage(), JLabel.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
            logger.exception("Error generating customer report", ex);
        }
        
        //close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(__ -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    //order management panel
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Order Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        //create table with selection model
        String[] columns = {"Order ID", "Customer", "Date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(new Object[0][4], columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        JTable ordersTable = new JTable(tableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        
        //table sorting capability
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        ordersTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        //search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchOrders(searchField.getText(), sorter); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchOrders(searchField.getText(), sorter); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchOrders(searchField.getText(), sorter); }
        });
        
        //button panel
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton createButton = new JButton("Create Order");
        JButton viewButton = new JButton("View Details");
        JButton updateButton = new JButton("Update Status");
        JButton backButton = new JButton("Back to Main Menu");
        
        //initially disable buttons until a row is selected
        viewButton.setEnabled(false);
        updateButton.setEnabled(false);
        
        //action listeners
        refreshButton.addActionListener(__ -> refreshOrdersTable(ordersTable));
        createButton.addActionListener(__ -> createNewOrder(ordersTable));
        viewButton.addActionListener(__ -> {
            int row = ordersTable.getSelectedRow();
            if (row >= 0) {
                row = ordersTable.convertRowIndexToModel(row);
                String orderId = (String) tableModel.getValueAt(row, 0);
                viewOrderDetails(orderId);
            }
        });
        updateButton.addActionListener(__ -> {
            int row = ordersTable.getSelectedRow();
            if (row >= 0) {
                row = ordersTable.convertRowIndexToModel(row);
                String orderId = (String) tableModel.getValueAt(row, 0);
                updateOrderStatus(orderId, ordersTable);
            }
        });
        backButton.addActionListener(__ -> cardLayout.show(mainPanel, "MainMenu"));
        
        //enable/disable buttons based on selection
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = ordersTable.getSelectedRow() >= 0;
            viewButton.setEnabled(rowSelected);
            updateButton.setEnabled(rowSelected);
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(createButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(backButton);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(searchPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        //load orders initially
        refreshOrdersTable(ordersTable);
        
        return panel;
    }
    
    //helper method for order search
    private void searchOrders(String text, TableRowSorter<TableModel> sorter) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            //case-insensitive search across all columns
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
    
    private void refreshOrdersTable(JTable table) {
        List<Order> orders = orderService.getAllOrders();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        //clear existing data
        model.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Order order : orders) {
            model.addRow(new Object[]{
                order.getId(),
                order.getCustomer().getName(),
                order.getOrderDate().format(formatter),
                order.getStatus()
            });
        }
        
        //inform user if no orders
        if (orders.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No orders found in the database. Create some orders to get started.",
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    //method to create a new order
    private void createNewOrder(JTable table) {
        //check if  customers and products
        List<Customer> customers = customerService.getAllCustomers();
        List<Product> products = productService.getAllProducts();
        
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No customers in the system. Please add a customer first.", 
                    "Cannot Create Order", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No products in the system. Please add some products first.", 
                    "Cannot Create Order", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        //create order dialog
        JDialog dialog = new JDialog(this, "Create New Order", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerPanel.add(new JLabel("Select Customer:"));
        
        JComboBox<CustomerItem> customerComboBox = new JComboBox<>();
        for (Customer customer : customers) {
            customerComboBox.addItem(new CustomerItem(customer));
        }
        customerPanel.add(customerComboBox);
        
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Order Items"));
        
        //table for selected items
        String[] itemColumns = {"Product", "Price", "Quantity", "Total"};
        DefaultTableModel itemsModel = new DefaultTableModel(itemColumns, 0);
        JTable itemsTable = new JTable(itemsModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        //panel for adding items
        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addItemPanel.add(new JLabel("Product:"));
        
        JComboBox<ProductItem> productComboBox = new JComboBox<>();
        for (Product product : products) {
            productComboBox.addItem(new ProductItem(product));
        }
        addItemPanel.add(productComboBox);
        
        addItemPanel.add(new JLabel("Quantity:"));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        addItemPanel.add(quantitySpinner);
        
        JButton addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(__ -> {
            ProductItem selectedProduct = (ProductItem) productComboBox.getSelectedItem();
            int quantity = (Integer) quantitySpinner.getValue();
            
            if (selectedProduct != null && quantity > 0) {
                //check stock
                if (quantity > selectedProduct.product.getStock()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Not enough stock. Available: " + selectedProduct.product.getStock(),
                            "Insufficient Stock",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                double totalPrice = selectedProduct.product.getPrice() * quantity;
                
                itemsModel.addRow(new Object[]{
                        selectedProduct.toString(),
                        String.format("$%.2f", selectedProduct.product.getPrice()),
                        quantity,
                        String.format("$%.2f", totalPrice)
                });
            }
        });
        addItemPanel.add(addItemButton);
        
        JButton removeItemButton = new JButton("Remove Item");
        removeItemButton.addActionListener(__ -> {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow >= 0) {
                itemsModel.removeRow(selectedRow);
            }
        });
        addItemPanel.add(removeItemButton);
        
        itemsPanel.add(addItemPanel, BorderLayout.SOUTH);
        
        //order summary
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel("Total: $0.00");
        summaryPanel.add(totalLabel);
        
        //update total when items change
        itemsModel.addTableModelListener(e -> {
            double total = 0;
            for (int i = 0; i < itemsModel.getRowCount(); i++) {
                String totalPrice = (String) itemsModel.getValueAt(i, 3);
                total += Double.parseDouble(totalPrice.substring(1)); // Remove $ sign
            }
            totalLabel.setText(String.format("Total: $%.2f", total));
        });
        
        //order button
        JButton createOrderButton = new JButton("Create Order");
        createOrderButton.addActionListener(__ -> {
            try {
                if (itemsModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please add at least one item to the order",
                            "Empty Order",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                //get selected customer
                CustomerItem selectedCustomer = (CustomerItem) customerComboBox.getSelectedItem();
                
                //generate order ID
                String orderId = "O" + UUID.randomUUID().toString().substring(0, 8);
                
                //create order
                Order order = new Order(orderId, selectedCustomer.customer);
                
                //add items to order
                for (int i = 0; i < itemsModel.getRowCount(); i++) {
                    String productString = (String) itemsModel.getValueAt(i, 0);
                    int qty = Integer.parseInt(itemsModel.getValueAt(i, 2).toString());
                    
                    //find product from the selection
                    for (int j = 0; j < productComboBox.getItemCount(); j++) {
                        ProductItem productItem = productComboBox.getItemAt(j);
                        if (productItem.toString().equals(productString)) {
                            order.addItem(productItem.product, qty);
                            break;
                        }
                    }
                }
                
                //save order
                orderService.createOrder(order);
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, 
                        "Order created successfully with ID: " + orderId,
                        "Order Created",
                        JOptionPane.INFORMATION_MESSAGE);
                
                //refresh the orders table
                refreshOrdersTable(table);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error creating order: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                logger.exception("Error creating order", ex);
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(__ -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createOrderButton);
        buttonPanel.add(cancelButton);
        
        panel.add(customerPanel, BorderLayout.NORTH);
        panel.add(itemsPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(summaryPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    //helper class for customer combo box
    private class CustomerItem {
        final Customer customer;
        
        CustomerItem(Customer customer) {
            this.customer = customer;
        }
        
        @Override
        public String toString() {
            return customer.getName() + " (" + customer.getId() + ")";
        }
    }
    
    //helper class for product combo box
    private class ProductItem {
        final Product product;
        
        ProductItem(Product product) {
            this.product = product;
        }
        
        @Override
        public String toString() {
            return product.getName() + " - $" + product.getPrice();
        }
    }
    
    //method to view order details
    private void viewOrderDetails(String orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            
            if (order == null) {
                JOptionPane.showMessageDialog(this, 
                        "Order not found", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JDialog dialog = new JDialog(this, "Order Details - " + orderId, true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            
            //order header
            JPanel headerPanel = new JPanel(new GridLayout(4, 2, 10, 5));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            headerPanel.add(new JLabel("Order ID:"));
            headerPanel.add(new JLabel(order.getId()));
            
            headerPanel.add(new JLabel("Customer:"));
            headerPanel.add(new JLabel(order.getCustomer().getName()));
            
            headerPanel.add(new JLabel("Date:"));
            headerPanel.add(new JLabel(order.getOrderDate().format(formatter)));
            
            headerPanel.add(new JLabel("Status:"));
            headerPanel.add(new JLabel(order.getStatus().toString()));
            
            panel.add(headerPanel, BorderLayout.NORTH);
            
            //order items
            String[] columns = {"Product", "Price", "Quantity", "Total"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable itemsTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(itemsTable);
            
            double orderTotal = 0;
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                double itemTotal = product.getPrice() * item.getQuantity();
                orderTotal += itemTotal;
                
                model.addRow(new Object[]{
                        product.getName(),
                        String.format("$%.2f", product.getPrice()),
                        item.getQuantity(),
                        String.format("$%.2f", itemTotal)
                });
            }
            
            panel.add(scrollPane, BorderLayout.CENTER);
            
            //order total
            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            totalPanel.add(new JLabel("Total: " + String.format("$%.2f", orderTotal)));
            
            //close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(__ -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            
            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.add(totalPanel, BorderLayout.NORTH);
            southPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            panel.add(southPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error loading order details: " + ex.getMessage(), 
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            logger.exception("Error loading order details", ex);
        }
    }
    
    //method to update order status
    private void updateOrderStatus(String orderId, JTable table) {
        try {
            Order order = orderService.getOrder(orderId);
            
            if (order == null) {
                JOptionPane.showMessageDialog(this, 
                        "Order not found", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String[] statusOptions = {
                OrderStatus.NEW.toString(),
                OrderStatus.PROCESSING.toString(),
                OrderStatus.COMPLETED.toString(),
                OrderStatus.CANCELLED.toString()
            };
            
            String currentStatus = order.getStatus().toString();
            
            String selectedStatus = (String) JOptionPane.showInputDialog(
                    this,
                    "Select new status for order " + orderId,
                    "Update Order Status",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    statusOptions,
                    currentStatus);
            
            if (selectedStatus != null && !selectedStatus.equals(currentStatus)) {
                OrderStatus newStatus = OrderStatus.valueOf(selectedStatus);
                orderService.updateOrderStatus(orderId, newStatus);
                JOptionPane.showMessageDialog(this, 
                        "Order status updated successfully!",
                        "Status Updated",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshOrdersTable(table);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error updating order status: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            logger.exception("Error updating order status", ex);
        }
    }
    
}
