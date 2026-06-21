package com.fittrack.fittrackbackend.nutrition.exception;

public class FoodNotFoundException extends RuntimeException{
    public FoodNotFoundException(String message){
        super(message);
    }

}
