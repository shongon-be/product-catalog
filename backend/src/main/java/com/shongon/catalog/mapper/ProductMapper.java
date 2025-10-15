package com.shongon.catalog.mapper;

import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.CreateProductResponse;
import com.shongon.catalog.dto.response.GetProductResponse;
import com.shongon.catalog.dto.response.UpdateProductResponse;
import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.model.Product;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", expression = "java(new org.bson.types.ObjectId())")
    Product createProduct(CreateProductRequest request);
    @Mapping(target = "id", ignore = true)
    void updateProduct(@MappingTarget Product product, UpdateProductRequest request);

    @Mapping(target = "message", constant = "Create product successfully!")
    CreateProductResponse toCreateProductResponse(Product product);

    @Mapping(target = "message", constant = "Update product successfully!")
    UpdateProductResponse toUpdateProductResponse(Product product);

    ViewAllProductsResponse toViewAllProductsResponse(Product product);

    GetProductResponse toGetProductResponse(Product product);

    default String map(ObjectId objectId) {
        return objectId != null ? objectId.toString() : null;
    }
}
