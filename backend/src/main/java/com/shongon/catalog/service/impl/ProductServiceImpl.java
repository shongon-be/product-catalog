package com.shongon.catalog.service.impl;

import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.CreateProductResponse;
import com.shongon.catalog.dto.response.GetProductResponse;
import com.shongon.catalog.dto.response.UpdateProductResponse;
import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.exception.ErrorCode;
import com.shongon.catalog.exception.ProductCatalogException;
import com.shongon.catalog.mapper.ProductMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import com.shongon.catalog.service.IProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements IProductService {

    ProductRepository productRepository;
    ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ViewAllProductsResponse> viewAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toViewAllProductsResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GetProductResponse getProductById(String productId) {
        // Convert String to ObjectId
        ObjectId objectId = new ObjectId(productId);

        return productRepository.findById(objectId)
                .map(productMapper::toGetProductResponse)
                .orElseThrow(() -> new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        validateUniqueProductName(request.getName());

        Product product = productMapper.createProduct(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toCreateProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public UpdateProductResponse updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product with id: {}", productId);

        ObjectId objectId = new ObjectId(productId);

        Product existingProduct = productRepository.findById(objectId)
                .orElseThrow(() -> new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));

        productMapper.updateProduct(existingProduct, request);

        log.info("Product updated successfully with id: {}", productId);
        return productMapper.toUpdateProductResponse(productRepository.save(existingProduct));
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        Product productToDelete = productRepository.findById(new ObjectId(productId))
                .orElseThrow(() -> new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(productToDelete);
        log.info("Product deleted successfully with id: {}", productId);
    }

    private void validateUniqueProductName(String productName){
        if(productRepository.existsByName(productName)){
            throw new ProductCatalogException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
    }
}
