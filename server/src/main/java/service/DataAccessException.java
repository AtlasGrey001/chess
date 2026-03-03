package service;

public class DataAccessException extends RuntimeException {
    public DataAccessException(String txt) {
        super(txt);
    }
}
