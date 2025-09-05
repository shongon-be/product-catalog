package com.shongon.catalog.service;

import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.enums.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ISortFilterService {
    Page<ViewAllProductsResponse> filterAndSortProducts(
            String category,
            SortField sortBy,
            Sort.Direction direction,
            Pageable pageable
    );
}
