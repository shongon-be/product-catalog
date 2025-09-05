package com.shongon.catalog.enums;

import lombok.Getter;

@Getter
public enum SortField {
    NAME("name"),
    PRICE("price");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }
}