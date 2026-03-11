package service.exceptions;

public class DataAccessException extends RuntimeException {
    public DataAccessException(String txt) {
        super(txt);
    }
}
