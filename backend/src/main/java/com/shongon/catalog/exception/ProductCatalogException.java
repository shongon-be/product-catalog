package com.shongon.catalog.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCatalogException extends RuntimeException {
    private final ErrorCode errorCode;

    public ProductCatalogException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
