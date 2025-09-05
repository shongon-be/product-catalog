package com.shongon.catalog.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mongodb.DuplicateKeyException;
import com.shongon.catalog.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductCatalogException.class)
    public ResponseEntity<ApiErrorResponse> handleProductCatalogException(ProductCatalogException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .code(errorCode.getCode())
                .errors(List.of(errorCode.getMessage()))
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUncategorized(Exception ex) {
        log.error("Uncategorized error", ex);
        return ResponseEntity
                .status(ErrorCode.UNCATEGORIZED.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.UNCATEGORIZED.getCode(), List.of(ErrorCode.UNCATEGORIZED.getMessage())));
    }

    // 400 – @Valid body: field errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .distinct()
                .collect(toList());

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.VALIDATION_FAILED.getCode(), errors));
    }

    // 400 – @Validated on params/path: constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(toList());

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.VALIDATION_FAILED.getCode(), errors));
    }

    // 400 – JSON lỗi/kiểu sai (ví dụ price: "abc")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String msg = ErrorCode.JSON_PARSE_ERROR.getMessage();
        if (ex.getCause() instanceof InvalidFormatException ife) {
            // Gợi ý field sai kiểu
            String path = ife.getPath().stream().map(JsonMappingException.Reference::getFieldName).reduce((a, b) -> a + "." + b).orElse("");
            msg = "Invalid value for field '" + path + "'";
        }
        return ResponseEntity
                .status(ErrorCode.JSON_PARSE_ERROR.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.JSON_PARSE_ERROR.getCode(), List.of(msg)));
    }

    // 400 – Sai kiểu param/path variable
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Parameter '%s' has invalid value '%s'", ex.getName(), ex.getValue());
        return ResponseEntity
                .status(ErrorCode.TYPE_MISMATCH.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.TYPE_MISMATCH.getCode(), List.of(msg)));
    }

    // 400 – Thiếu param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = String.format("Missing required parameter '%s'", ex.getParameterName());
        return ResponseEntity
                .status(ErrorCode.MISSING_PARAMETER.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.MISSING_PARAMETER.getCode(), List.of(msg)));
    }

    // 405 – Sai method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED.getCode(), List.of(ErrorCode.METHOD_NOT_ALLOWED.getMessage())));
    }

    // 409 – Key trùng (Mongo unique index)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateKey(DuplicateKeyException ex) {
        return ResponseEntity
                .status(ErrorCode.PRODUCT_ALREADY_EXISTS.getStatusCode())
                .body(ApiErrorResponse.of(ErrorCode.PRODUCT_ALREADY_EXISTS.getCode(), List.of(ErrorCode.PRODUCT_ALREADY_EXISTS.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidObjectId(IllegalArgumentException ex) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .code(400)
                .errors(List.of("Invalid productId format. Must be a valid Mongo ObjectId"))
                .build();

        return ResponseEntity.badRequest().body(response);
    }
}
