package service;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String txt) {
        super(txt);
    }
}
