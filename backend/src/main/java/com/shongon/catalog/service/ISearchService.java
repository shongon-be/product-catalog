package com.shongon.catalog.service;

import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import org.springframework.data.domain.Page;


public interface ISearchService {
    Page<ViewAllProductsResponse> searchProducts(
            String keyword, int page, int size, String sortBy, String direction
    );
}
