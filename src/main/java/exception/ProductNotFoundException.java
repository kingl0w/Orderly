package main.java.exception;

public class ProductNotFoundException extends OrderlyException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
