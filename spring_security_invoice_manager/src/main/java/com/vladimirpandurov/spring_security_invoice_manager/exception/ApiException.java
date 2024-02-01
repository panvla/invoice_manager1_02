package com.vladimirpandurov.spring_security_invoice_manager.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message){
        super(message);
    }
}
