package com.shongon.catalog.controller;

import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.*;
import com.shongon.catalog.enums.SortField;
import com.shongon.catalog.service.IProductService;
import com.shongon.catalog.service.ISortFilterService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    IProductService productService;
    ISortFilterService sortFilterService;

    // CRUD operations
    @GetMapping
    public ApiResponse<Page<ViewAllProductsResponse>> getAllProducts(Pageable pageable) {
        return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                .code(200)
                .message("Success")
                .result(productService.viewAllProducts(pageable))
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<GetProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<GetProductResponse>builder()
                .code(200)
                .message("Success")
                .result(productService.getProductById(productId))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateProductResponse> createProduct(
            @RequestBody @Valid CreateProductRequest request
    ) {
        return ApiResponse.<CreateProductResponse>builder()
                .code(201)
                .message("Success")
                .result(productService.createProduct(request))
                .build();
    }

    @PutMapping("/{productId}")
    public ApiResponse<UpdateProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        return ApiResponse.<UpdateProductResponse>builder()
                .code(200)
                .message("Success")
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Filter and Sort operations (MongoDB)
    @GetMapping("/filter")
    public ApiResponse<Page<ViewAllProductsResponse>> filterAndSortProduct(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "") SortField field,
            @RequestParam(defaultValue = "") Sort.Direction direction,
            Pageable pageable
    ){
        log.info("category: {}, field: {}, direction: {}", category, field, direction);

        return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                .code(200)
                .message("Success")
                .result(sortFilterService.filterAndSortProducts(category,field, direction, pageable))
                .build();
    }
}
