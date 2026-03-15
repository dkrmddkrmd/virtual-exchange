package com.example.virtual_exchange.exception;

public class UpbitApiCallException extends RuntimeException {
    public UpbitApiCallException(String message) {
        super(message);
    }
}
