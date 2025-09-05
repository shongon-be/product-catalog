package com.shongon.catalog.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // Generic
    UNCATEGORIZED(500, HttpStatus.INTERNAL_SERVER_ERROR, "Uncategorized Error"),
    INVALID_KEY(400, HttpStatus.BAD_REQUEST, "Invalid Message Key"),

    // Validation / request errors
    VALIDATION_FAILED(400, HttpStatus.BAD_REQUEST, "Validation failed"),
    JSON_PARSE_ERROR(400, HttpStatus.BAD_REQUEST, "Malformed or invalid JSON"),
    TYPE_MISMATCH(400, HttpStatus.BAD_REQUEST, "Parameter type mismatch"),
    MISSING_PARAMETER(400, HttpStatus.BAD_REQUEST, "Missing required parameter"),
    METHOD_NOT_ALLOWED(405, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(415, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),

    // Domain
    PRODUCT_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Product not found"),
    PRODUCT_ALREADY_EXISTS(409, HttpStatus.CONFLICT, "Product already exists")
    ;


    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;

    ErrorCode(int code, HttpStatusCode statusCode, String message) {
        this.code = code;
        this.statusCode = statusCode;
        this.message = message;
    }
}
