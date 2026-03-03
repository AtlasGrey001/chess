package service;

public class AlreadyTakenException extends RuntimeException {
    public AlreadyTakenException(String txt) {
        super(txt);
    }
}
