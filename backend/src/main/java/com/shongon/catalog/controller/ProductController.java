package com.shongon.catalog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shongon.catalog.dto.cache.CacheablePage;
import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.*;
import com.shongon.catalog.enums.SortField;
import com.shongon.catalog.service.ICacheService;
import com.shongon.catalog.service.IProductService;
import com.shongon.catalog.service.ISearchService;
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

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    IProductService productService;
    ISortFilterService sortFilterService;
    ICacheService cacheService;
    ISearchService searchService;

    // CRUD operations
    @GetMapping
    public ApiResponse<Page<ViewAllProductsResponse>> getAllProducts(Pageable pageable) {
        // Generate cache key
        String cacheKey = cacheService.generateCacheKey(
                "all",
                "page", pageable.getPageNumber(),
                "size", pageable.getPageSize()
        );

        // Try to get from cache
        CacheablePage<ViewAllProductsResponse> cachedResult = cacheService.getFromCache(
                cacheKey,
                new TypeReference<>() {
                }
        );

        if (cachedResult != null) {
            log.info("Cache HIT - Returning cached result for getAllProducts");
            return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                    .code(200)
                    .message("Success (Cached)")
                    .result(cachedResult.toPage(pageable))
                    .build();
        }

        // Cache MISS - Get from database
        log.info("Cache MISS - Fetching from database");
        Page<ViewAllProductsResponse> response = productService.viewAllProducts(pageable);

        // Save to cache (convert to CacheablePage first)
        CacheablePage<ViewAllProductsResponse> cacheableResult = CacheablePage.from(response);
        cacheService.saveToCache(cacheKey, cacheableResult, Duration.ofMinutes(10));

        return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                .code(200)
                .message("Success")
                .result(response)
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
        CreateProductResponse response = productService.createProduct(request);

        invalidateProductCache();

        return ApiResponse.<CreateProductResponse>builder()
                .code(201)
                .message("Success")
                .result(response)
                .build();
    }

    @PutMapping("/{productId}")
    public ApiResponse<UpdateProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        UpdateProductResponse response = productService.updateProduct(productId, request);

        invalidateProductCache();

        return ApiResponse.<UpdateProductResponse>builder()
                .code(200)
                .message("Success")
                .result(response)
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);

        invalidateProductCache();

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Filter and Sort operations (MongoDB)
    @GetMapping("/filter")
    public ApiResponse<Page<ViewAllProductsResponse>> filterProductsByCategory(
            @RequestParam(required = false) String category,
            Pageable pageable
    ) {
        // Only cache if filtering by category (no sort parameters)
        if (category != null && !category.isBlank()) {
            String cacheKey = cacheService.generateCacheKey(
                    "filter", category,
                    "page", pageable.getPageNumber(),
                    "size", pageable.getPageSize()
            );

            // Try to get from cache
            CacheablePage<ViewAllProductsResponse> cachedResult = cacheService.getFromCache(
                    cacheKey,
                    new TypeReference<>() {
                    }
            );

            if (cachedResult != null) {
                log.info("Cache HIT - Returning cached result for category filter: {}", category);
                return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                        .code(200)
                        .message("Success (Cached)")
                        .result(cachedResult.toPage(pageable))
                        .build();
            }

            // Cache MISS - Get from database
            log.info("Cache MISS - Fetching filtered data from database for category: {}", category);
            Page<ViewAllProductsResponse> result = sortFilterService.filterAndSortProducts(
                    category, null, null, pageable
            );

            // Save to cache (convert to CacheablePage first)
            CacheablePage<ViewAllProductsResponse> cacheableResult = CacheablePage.from(result);
            cacheService.saveToCache(cacheKey, cacheableResult, Duration.ofMinutes(10));

            return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                    .code(200)
                    .message("Success")
                    .result(result)
                    .build();
        }
        // If no category filter, just return normal viewAll (which has its own cache)
        return getAllProducts(pageable);
    }

    @GetMapping("/sort")
    public ApiResponse<Page<ViewAllProductsResponse>> filterAndSortProduct(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "") SortField field,
            @RequestParam(defaultValue = "") Sort.Direction direction,
            Pageable pageable
    ) {
        log.info("category: {}, field: {}, direction: {}", category, field, direction);

        Page<ViewAllProductsResponse> result = sortFilterService.filterAndSortProducts(
                category, field, direction, pageable
        );

        return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                .code(200)
                .message("Success")
                .result(result)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<ViewAllProductsResponse>> searchProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("keyword: {}", keyword);

        Page<ViewAllProductsResponse> result = searchService.searchProducts(keyword, page, size, null, null);

        return ApiResponse.<Page<ViewAllProductsResponse>>builder()
                .code(200)
                .message("Success")
                .result(result)
                .build();
    }


    // HELPER PRIVATE METHOD
    private void invalidateProductCache() {
        String cachePattern = cacheService.generateCacheKey("*");
        cacheService.evictCacheByPattern(cachePattern);
        log.info("All product cache invalidated due to data modification");
    }
}
