package service;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String txt) {
        super(txt);
    }
}
