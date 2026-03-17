package com.example.virtual_exchange.exception;

public class KafkaProducerErrorException extends RuntimeException {
    public KafkaProducerErrorException(String message) {
        super(message);
    }
}
