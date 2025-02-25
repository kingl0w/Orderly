package main.java.exception;

public class InvalidOrderException extends OrderlyException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
