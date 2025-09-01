package com.shongon.catalog.service.impl;

import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.enums.SortField;
import com.shongon.catalog.mapper.ProductMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.service.ISortFilterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SortFilterServiceImpl implements ISortFilterService {
    MongoTemplate mongoTemplate;
    ProductMapper productMapper;

    @Override
    public Page<ViewAllProductsResponse> filterAndSortProducts(
            String category,
            SortField sortBy,
            Sort.Direction direction,
            Pageable pageable
    ) {
        // Create empty query object
        Query query = new Query();

        // Filter
        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").is(category));
        } // Add filter query with {"category": categoryType} to query object

        // Sort
        if (sortBy != null && direction != null) {
            query.with(Sort.by(direction, sortBy.getFieldName()));
        } // Add sort query with {"sortBy": sortBy, 1(ASC)/-1(DESC)} to query object

        // Pagination
        query.with(pageable); // Passing params "page" and "size" to query object
        /* -> PageRequest.of(page, size)
            Mongo recognite it to skip & limit
                skip = pageNumber * pageSize
                limit = pageSize
            Ex: [D1, D2, D3, D4, D5, D6, D7, D8, D9, D10]
                Page 0 (skip=0, limit=3) → [D1, D2, D3]
                Page 1 (skip=3, limit=3) → [D4, D5, D6]
                Page 2 (skip=6, limit=3) → [D7, D8, D9]
                Page 3 (skip=9, limit=3) → [D10]
         */

        // Execute query return Product's list
        var products = mongoTemplate.find(query, Product.class);
        /*
            mongo.products.find(
                {}, // return all products matching the query
                {
                    "category": categotyType, (nullable = true)
                    "sortBy": sortBy, (nullable = true)
                    "direction": direction (nullable = true)
                    "page": 0, (default = 0)
                    "size": 10 (default = 10)
                }
            )
         */

        // Count total documents fit with filter (except pagination) -> 'totalElements' in Page
        long total = mongoTemplate.count(
                Query.of(query) // clone query object
                        .skip(-1).limit(-1) // set page & size to default, ignore pagination to count concise total
                , Product.class);

        // Map to response
        var response = products.stream()
                .map(productMapper::toViewAllProductsResponse)
                .toList();

        return new PageImpl<>(response, pageable, total);
    }
}
