package com.shongon.catalog.service.impl;

import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.mapper.ProductMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import com.shongon.catalog.service.ISearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchServiceImpl implements ISearchService {
    ProductMapper productMapper;
    ProductRepository productRepository;

    @Override
    public Page<ViewAllProductsResponse> searchProducts(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            productPage = productRepository.findAll(pageable);
        } else {
            productPage = productRepository.searchProducts(keyword.trim(), pageable);
        }

        return productPage.map(productMapper::toViewAllProductsResponse);
    }
}
