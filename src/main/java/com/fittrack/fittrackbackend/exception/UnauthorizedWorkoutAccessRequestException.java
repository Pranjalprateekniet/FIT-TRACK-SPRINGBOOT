package com.fittrack.fittrackbackend.exception;

public class UnauthorizedWorkoutAccessRequestException extends RuntimeException{
    public UnauthorizedWorkoutAccessRequestException(String message){
        super(message);
    }
}
