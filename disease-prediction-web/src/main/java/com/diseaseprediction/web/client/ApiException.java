package com.diseaseprediction.web.client;

public class ApiException extends Exception {
    private final int statusCode;
    private final String error;

    public ApiException(int statusCode, String error, String message) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
    }

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.error = "API Error";
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
        this.error = "Connection Error";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }
}
