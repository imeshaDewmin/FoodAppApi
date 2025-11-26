package com.selfstudy.foodapp.exceptions;

public class NotFoundException extends RuntimeException{
    public NotFoundException (String message){
        super(message);
    }

    public class PaymentProcessingException extends RuntimeException{
        public PaymentProcessingException(String message){

            super(message);
        }
    }
}
