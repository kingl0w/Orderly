package test.java.model;

import main.java.model.Product;

//simple test of Product class
public class ProductTest {
    public static void main(String[] args) {
        testProductBuilder();
        testProductClone();
        System.out.println("All tests passed!");
    }

    private static void testProductBuilder() {
        Product product = new Product.Builder()
                .id("P001")
                .name("Test Product")
                .price(19.99)
                .description("Test Description")
                .stock(10)
                .build();

        //manual assertions
        assert "P001".equals(product.getId()) : "Product ID doesn't match";
        assert "Test Product".equals(product.getName()) : "Product name doesn't match";
        assert Math.abs(19.99 - product.getPrice()) < 0.001 : "Product price doesn't match";
        assert "Test Description".equals(product.getDescription()) : "Product description doesn't match";
        assert 10 == product.getStock() : "Product stock doesn't match";
    }

    private static void testProductClone() {
        Product original = new Product.Builder()
                .id("P001")
                .name("Original Product")
                .price(19.99)
                .build();

        Product cloned = original.clone();
        assert cloned != null : "Cloned product is null";
        assert original.getId().equals(cloned.getId()) : "Cloned product ID doesn't match";
        assert original.getName().equals(cloned.getName()) : "Cloned product name doesn't match";
        assert Math.abs(original.getPrice() - cloned.getPrice()) < 0.001 : "Cloned product price doesn't match";
    }
}