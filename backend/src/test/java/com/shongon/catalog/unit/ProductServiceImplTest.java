package com.shongon.catalog.unit;

import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.CreateProductResponse;
import com.shongon.catalog.dto.response.GetProductResponse;
import com.shongon.catalog.dto.response.UpdateProductResponse;
import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.exception.ProductCatalogException;
import com.shongon.catalog.mapper.ProductMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import com.shongon.catalog.service.impl.ProductServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private String productId;
    private Product product;
    private CreateProductRequest createProductRequest;
    private UpdateProductRequest updateProductRequest;

    @BeforeEach
    void setUp() {
        productId = "68ad8b8f1f76bd5e1eb753cd";

        product = Product.builder()
                .id(new ObjectId(productId))
                .name("Test Food Product")
                .description("Test Food Description")
                .price(10.0)
                .category("FOOD")
                .build();

        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("New Clothing Product");
        createProductRequest.setDescription("New Clothing Description");
        createProductRequest.setPrice(100.0);
        createProductRequest.setCategory("CLOTHING");

        updateProductRequest = new UpdateProductRequest();
        updateProductRequest.setName("New Food Product");
        updateProductRequest.setDescription("New Food Description");
        updateProductRequest.setPrice(1000.0);
        updateProductRequest.setCategory("FOOD");
    }

    @Test
    void viewAllProducts_shouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        ViewAllProductsResponse response = new ViewAllProductsResponse();

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);
        when(productMapper.toViewAllProductsResponse(any()))
                .thenReturn(response);

        Page<ViewAllProductsResponse> result = productService.viewAllProducts(pageable);

        assertThat(result).isNotNull();

        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void viewProductById_shouldReturnProduct() {
        GetProductResponse response = new GetProductResponse();

        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.of(product));
        when(productMapper.toGetProductResponse(product))
                .thenReturn(response);

        GetProductResponse result = productService.getProductById(productId);

        assertThat(result).isNotNull();

        verify(productRepository, times(1))
                .findById(any(ObjectId.class));
    }

    @Test
    void viewProductById_whenProductIdInvalid_shouldThrowException() {
        String invalidProductId = "123";

        assertThrows(IllegalArgumentException.class, () ->
                productService.getProductById(invalidProductId)
        );

        verify(productRepository, never()).findById(any(ObjectId.class));
    }

    @Test
    void viewProductById_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.empty());

        assertThrows(ProductCatalogException.class, () ->
                productService.getProductById(productId)
        );

        verify(productRepository, times(1))
                .findById(any(ObjectId.class));
    }

    @Test
    void createProduct_shouldReturnProduct() {
        when(productRepository.existsByName(anyString()))
                .thenReturn(false);
        when(productMapper.createProduct(createProductRequest))
                .thenReturn(product);
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);
        when(productMapper.toCreateProductResponse(product))
                .thenReturn(new CreateProductResponse());

        CreateProductResponse result = productService.createProduct(createProductRequest);

        assertThat(result).isNotNull();

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void createProduct_whenNameExists_shouldThrowException() {
        when(productRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(ProductCatalogException.class, () ->
                productService.createProduct(createProductRequest)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldReturnProduct() {
        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.of(product));
        when(productRepository.existsByName(anyString()))
                .thenReturn(false);
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);
        when(productMapper.toUpdateProductResponse(product))
                .thenReturn(new UpdateProductResponse());
        UpdateProductResponse result = productService.updateProduct(productId, updateProductRequest);

        assertThat(result).isNotNull();
        verify(productMapper, times(1))
                .updateProduct(any(Product.class), eq(updateProductRequest));
        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void updateProduct_whenNameExists_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.of(product));
        when(productRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(ProductCatalogException.class, () ->
                productService.updateProduct(productId, updateProductRequest)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.empty());

        assertThrows(ProductCatalogException.class, () ->
                productService.updateProduct(productId, updateProductRequest)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductIdInvalid_shouldThrowException() {
        String invalidProductId = "123";

        assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(invalidProductId, updateProductRequest)
        );

        verify(productRepository, never()).findById(any(ObjectId.class));
    }

    @Test
    void deleteProduct_shouldDeleteProduct() {
        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository, times(1))
                .delete(any(Product.class));
    }

    @Test
    void deleteProduct_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class)))
                .thenReturn(Optional.empty());

        assertThrows(ProductCatalogException.class, () ->
                productService.deleteProduct(productId)
        );

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteProduct_whenProductIdInvalid_shouldThrowException() {
        String invalidProductId = "123";

        assertThrows(IllegalArgumentException.class, () ->
                productService.deleteProduct(invalidProductId)
        );

        verify(productRepository, never()).findById(any(ObjectId.class));
    }
}
