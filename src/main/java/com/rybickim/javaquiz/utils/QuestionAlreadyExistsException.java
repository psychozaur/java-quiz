package com.rybickim.javaquiz.utils;

public class QuestionAlreadyExistsException extends RuntimeException {

    public QuestionAlreadyExistsException(String message) {
        super(message);
    }
}
