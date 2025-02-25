package main.java.exception;

public class InsufficientStockException extends OrderlyException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
