package de.clinc8686.hochschul_crawler;

public class TooManyFalseLoginException extends Exception {
    TooManyFalseLoginException() {};

    public TooManyFalseLoginException(String message) {
        super(message);
    }
}
