package com.shongon.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@Builder
public class ApiErrorResponse {
    private int code;
    private List<String> errors;
}
