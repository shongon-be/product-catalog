package com.shongon.catalog.service;

import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    // Read operations
    Page<ViewAllProductsResponse> viewAllProducts(Pageable pageable);
    GetProductResponse getProductById(String productId);

    // Write operations
    CreateProductResponse createProduct(CreateProductRequest request);
    UpdateProductResponse updateProduct(String productId, UpdateProductRequest request);
    void deleteProduct(String productId);

}
