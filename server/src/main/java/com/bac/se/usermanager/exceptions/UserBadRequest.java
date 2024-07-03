package com.bac.se.usermanager.exceptions;

public class UserBadRequest extends RuntimeException{
    public UserBadRequest(String message) {
        super(message);
    }
}
