package main.java.service;

import main.java.dao.ProductDAO;
import main.java.exception.ProductNotFoundException;
import main.java.model.Product;
import main.java.util.LoggerUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * productservice handles business logic for product operations.
 * acts as intermediary between ui and dao layers.
 * 
 * future enhancements:
 * - add validation rules
 * - implement product categories
 * - add inventory management
 */
public class ProductService {
    private final ProductDAO productDAO;
    private final LoggerUtil logger;

    public ProductService() {
        this.productDAO = ProductDAO.getInstance();
        this.logger = LoggerUtil.getInstance();
        logger.info("ProductService initialized");
    }

    public void createProduct(Product product) {
        logger.info("Creating product: " + product.getId());
        
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            logger.warning("Attempted to create product with no name: " + product.getId());
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        
        if (product.getPrice() < 0) {
            logger.warning("Attempted to create product with negative price: " + product.getId());
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        
        if (product.getStock() < 0) {
            logger.warning("Attempted to create product with negative stock: " + product.getId());
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
        
        productDAO.save(product);
        logger.info("Product created successfully: " + product.getId());
    }

    public Product getProduct(String id) throws ProductNotFoundException {
        logger.info("Retrieving product: " + id);
        Product product = productDAO.findById(id);
        
        if (product == null) {
            logger.warning("Product not found: " + id);
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        
        return product;
    }

    public List<Product> getAllProducts() {
        logger.info("Retrieving all products");
        return productDAO.findAll();
    }
    
    public List<Product> searchProductsByName(String name) {
        logger.info("Searching products by name: " + name);
        List<Product> allProducts = productDAO.findAll();
        List<Product> matchingProducts = new ArrayList<>();
        
        String searchTerm = name.toLowerCase();
        
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(searchTerm)) {
                matchingProducts.add(product);
            }
        }
        
        logger.info("Found " + matchingProducts.size() + " products matching: " + name);
        return matchingProducts;
    }
    
    public List<Product> searchProductsByPriceRange(double minPrice, double maxPrice) {
        logger.info("Searching products by price range: " + minPrice + " - " + maxPrice);
        List<Product> allProducts = productDAO.findAll();
        List<Product> matchingProducts = new ArrayList<>();
        
        for (Product product : allProducts) {
            if (product.getPrice() >= minPrice && product.getPrice() <= maxPrice) {
                matchingProducts.add(product);
            }
        }
        
        logger.info("Found " + matchingProducts.size() + " products in price range: " + minPrice + " - " + maxPrice);
        return matchingProducts;
    }

    public void updateProduct(Product product) throws ProductNotFoundException {
        logger.info("Updating product: " + product.getId());
        
        //checks if product exists
        Product existingProduct = productDAO.findById(product.getId());
        if (existingProduct == null) {
            logger.warning("Attempted to update non-existent product: " + product.getId());
            throw new ProductNotFoundException("Product not found with ID: " + product.getId());
        }
        
        //validates product
        if (product.getPrice() < 0) {
            logger.warning("Attempted to update product with negative price: " + product.getId());
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        
        if (product.getStock() < 0) {
            logger.warning("Attempted to update product with negative stock: " + product.getId());
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
        
        productDAO.update(product);
        logger.info("Product updated successfully: " + product.getId());
    }

    public void deleteProduct(String id) throws ProductNotFoundException {
        logger.info("Deleting product: " + id);
        
        //checks if product exists
        Product existingProduct = productDAO.findById(id);
        if (existingProduct == null) {
            logger.warning("Attempted to delete non-existent product: " + id);
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        
        productDAO.delete(id);
        logger.info("Product deleted successfully: " + id);
    }
}
