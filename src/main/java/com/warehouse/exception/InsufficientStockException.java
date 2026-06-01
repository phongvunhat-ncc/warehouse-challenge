package com.warehouse.exception;

public class InsufficientStockException extends RuntimeException {
    private final String code;

    public InsufficientStockException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}