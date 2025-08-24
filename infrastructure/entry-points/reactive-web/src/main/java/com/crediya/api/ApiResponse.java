package com.crediya.api;

public class ApiResponse<T> {
    private String message;
    private T content;

    public ApiResponse(String message, T content) {
        this.message = message;
        this.content = content;
    }

    public ApiResponse(String message) {
        this.message = message;
    }

    public String getmessage() { return message; }
    public T getcontent() { return content; }
}