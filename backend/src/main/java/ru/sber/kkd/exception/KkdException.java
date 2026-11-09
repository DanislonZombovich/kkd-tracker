package ru.sber.kkd.exception;

import org.springframework.http.HttpStatus;

public class KkdException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public KkdException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static KkdException notFound(String code, String message) {
        return new KkdException(HttpStatus.NOT_FOUND, code, message);
    }

    public static KkdException invalidParam(String message) {
        return new KkdException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_PARAM", message);
    }

    public static KkdException conflict(String code, String message) {
        return new KkdException(HttpStatus.CONFLICT, code, message);
    }
}
