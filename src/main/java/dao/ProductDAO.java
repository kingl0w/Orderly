package main.java.dao;

import main.java.model.Product;
import main.java.util.LoggerUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * productdao handles data access operations for products.
 * implements singleton pattern for centralized data access.
 * 
 * future enhancements:
 * - add caching mechanism
 * - implement batch operations
 * - add search functionality
 */
public class ProductDAO {
    private static ProductDAO instance;
    private final DatabaseProxy db;
    private final LoggerUtil logger;

    private ProductDAO() {
        db = DatabaseProxy.getInstance();
        logger = LoggerUtil.getInstance();
        logger.info("ProductDAO initialized");
    }

    public static ProductDAO getInstance() {
        if (instance == null) {
            instance = new ProductDAO();
        }
        return instance;
    }

    public void save(Product product) {
        logger.info("Saving product: " + product.getId());
        db.save("product:" + product.getId(), product);
    }

    public Product findById(String id) {
        logger.info("Finding product by ID: " + id);
        return (Product) db.get("product:" + id);
    }

    public List<Product> findAll() {
        logger.info("Retrieving all products");
        List<Product> products = new ArrayList<>();
        
        //in a real implementation, we would query the database
        //for now, we just return an empty list
        return products;
    }
    
    public List<Product> findByName(String name) {
        logger.info("Finding products by name: " + name);
        List<Product> products = findAll();
        List<Product> result = new ArrayList<>();
        String searchName = name.toLowerCase();
        
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(searchName)) {
                result.add(product);
            }
        }
        
        return result;
    }
    
    public List<Product> findByPriceRange(double minPrice, double maxPrice) {
        logger.info("Finding products in price range: " + minPrice + " - " + maxPrice);
        List<Product> products = findAll();
        List<Product> result = new ArrayList<>();
        
        for (Product product : products) {
            if (product.getPrice() >= minPrice && product.getPrice() <= maxPrice) {
                result.add(product);
            }
        }
        
        return result;
    }
    
    public List<Product> findByStockLevel(int minStock) {
        logger.info("Finding products with stock >= " + minStock);
        List<Product> products = findAll();
        List<Product> result = new ArrayList<>();
        
        for (Product product : products) {
            if (product.getStock() >= minStock) {
                result.add(product);
            }
        }
        
        return result;
    }

    public void update(Product product) {
        logger.info("Updating product: " + product.getId());
        db.save("product:" + product.getId(), product);
    }

    public void delete(String id) {
        logger.info("Deleting product: " + id);
        db.delete("product:" + id);
    }
}