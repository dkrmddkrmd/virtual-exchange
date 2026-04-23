package com.example.virtual_exchange.exception;

public class AbnormalTradeException extends RuntimeException {
    public AbnormalTradeException(String message) {
        super(message);
    }
}
