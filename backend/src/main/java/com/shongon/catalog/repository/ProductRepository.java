package com.shongon.catalog.repository;

import com.shongon.catalog.model.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    boolean existsByName(String name);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Product> searchProducts(String keyword, Pageable pageable);
}
