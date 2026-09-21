package com.tms;

final class ApiException extends RuntimeException {
    final int status;
    final String key;
    ApiException(int status, String key, String message) {
        super(message);
        this.status = status;
        this.key = key;
    }
    static ApiException message(int status, String message) {
        return new ApiException(status, "message", message);
    }
}
