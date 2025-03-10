package main.java.dao;

import main.java.model.Product;
import main.java.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private final DatabaseManager dbManager;
    private final LoggerUtil logger;

    private ProductDAO() {
        dbManager = DatabaseManager.getInstance();
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
        String sql = "INSERT INTO products (id, name, price, description, stock) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getId());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getDescription());
            pstmt.setInt(5, product.getStock());
            
            pstmt.executeUpdate();
            logger.info("Product saved: " + product.getId());
            
        } catch (SQLException e) {
            logger.exception("Error saving product", e);
            throw new RuntimeException("Error saving product: " + e.getMessage(), e);
        }
    }

    public Product findById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding product by ID", e);
            throw new RuntimeException("Error finding product: " + e.getMessage(), e);
        }
        
        return null;
    }

    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
        } catch (SQLException e) {
            logger.exception("Error finding all products", e);
            throw new RuntimeException("Error retrieving products: " + e.getMessage(), e);
        }
        
        return products;
    }
    
    public List<Product> searchProductsByName(String name) {
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE ?";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name.toLowerCase() + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error searching products by name", e);
            throw new RuntimeException("Error searching products: " + e.getMessage(), e);
        }
        
        return products;
    }
    
    public List<Product> searchProductsByPriceRange(double minPrice, double maxPrice) {
        String sql = "SELECT * FROM products WHERE price BETWEEN ? AND ?";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, minPrice);
            pstmt.setDouble(2, maxPrice);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.exception("Error searching products by price range", e);
            throw new RuntimeException("Error searching products: " + e.getMessage(), e);
        }
        
        return products;
    }

    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, description = ?, stock = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getDescription());
            pstmt.setInt(4, product.getStock());
            pstmt.setString(5, product.getId());
            
            pstmt.executeUpdate();
            logger.info("Product updated: " + product.getId());
            
        } catch (SQLException e) {
            logger.exception("Error updating product", e);
            throw new RuntimeException("Error updating product: " + e.getMessage(), e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            logger.info("Product deleted: " + id);
            
        } catch (SQLException e) {
            logger.exception("Error deleting product", e);
            throw new RuntimeException("Error deleting product: " + e.getMessage(), e);
        }
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product.Builder()
                .id(rs.getString("id"))
                .name(rs.getString("name"))
                .price(rs.getDouble("price"))
                .description(rs.getString("description"))
                .stock(rs.getInt("stock"))
                .build();
    }
}