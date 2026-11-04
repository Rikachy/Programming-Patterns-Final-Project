package com.conference.exception;

public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean isUniqueViolation() { //for emails for example -> cant have duplicate emails
        Throwable t = getCause();
        return t != null && t.getMessage() != null
                && t.getMessage().contains("UNIQUE constraint failed");
    }
}