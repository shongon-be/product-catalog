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

    private final String VALID_ID = "68ad8b8f1f76bd5e1eb753cd";
    private final String INVALID_ID = "123";
    private Product product;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(new ObjectId(VALID_ID))
                .name("Test Food Product")
                .description("Test Food Description")
                .price(10.0)
                .category("FOOD")
                .build();

        createRequest = createProductRequest("New Clothing Product", "New Clothing Description", 100.0, "CLOTHING");
        updateRequest = createUpdateRequest("New Food Product", "New Food Description", 1000.0, "FOOD");
    }

    // Helper methods
    private CreateProductRequest createProductRequest(String name, String desc, Double price, String category) {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription(desc);
        request.setPrice(price);
        request.setCategory(category);
        return request;
    }

    private UpdateProductRequest createUpdateRequest(String name, String desc, Double price, String category) {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName(name);
        request.setDescription(desc);
        request.setPrice(price);
        request.setCategory(category);
        return request;
    }

    // VIEW ALL PRODUCTS TESTS
    @Test
    void viewAllProducts_shouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        ViewAllProductsResponse response = new ViewAllProductsResponse();

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toViewAllProductsResponse(any())).thenReturn(response);

        Page<ViewAllProductsResponse> result = productService.viewAllProducts(pageable);

        assertThat(result).isNotNull();
        verify(productRepository).findAll(pageable);
    }

    // GET PRODUCT BY ID TESTS
    @Test
    void viewProductById_shouldReturnProduct() {
        GetProductResponse response = new GetProductResponse();

        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(product));
        when(productMapper.toGetProductResponse(product)).thenReturn(response);

        GetProductResponse result = productService.getProductById(VALID_ID);

        assertThat(result).isNotNull();
        verify(productRepository).findById(any(ObjectId.class));
    }

    @Test
    void viewProductById_whenProductIdInvalid_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.getProductById(INVALID_ID));
        verify(productRepository, never()).findById(any(ObjectId.class));
    }

    @Test
    void viewProductById_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        assertThrows(ProductCatalogException.class,
                () -> productService.getProductById(VALID_ID));
        verify(productRepository).findById(any(ObjectId.class));
    }

    // CREATE PRODUCT TESTS
    @Test
    void createProduct_shouldReturnProduct() {
        when(productRepository.existsByName(anyString())).thenReturn(false);
        when(productMapper.createProduct(createRequest)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toCreateProductResponse(product)).thenReturn(new CreateProductResponse());

        CreateProductResponse result = productService.createProduct(createRequest);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_whenNameExists_shouldThrowException() {
        when(productRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(ProductCatalogException.class,
                () -> productService.createProduct(createRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    // UPDATE PRODUCT TESTS
    @Test
    void updateProduct_shouldReturnProduct() {
        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(product));
        when(productRepository.existsByName(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toUpdateProductResponse(product)).thenReturn(new UpdateProductResponse());

        UpdateProductResponse result = productService.updateProduct(VALID_ID, updateRequest);

        assertThat(result).isNotNull();
        verify(productMapper).updateProduct(any(Product.class), eq(updateRequest));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_whenNameExists_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(product));
        when(productRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(ProductCatalogException.class,
                () -> productService.updateProduct(VALID_ID, updateRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        assertThrows(ProductCatalogException.class,
                () -> productService.updateProduct(VALID_ID, updateRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductIdInvalid_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.updateProduct(INVALID_ID, updateRequest));
        verify(productRepository, never()).findById(any(ObjectId.class));
    }

    // DELETE PRODUCT TESTS
    @Test
    void deleteProduct_shouldDeleteProduct() {
        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(product));

        productService.deleteProduct(VALID_ID);

        verify(productRepository).delete(any(Product.class));
    }

    @Test
    void deleteProduct_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        assertThrows(ProductCatalogException.class,
                () -> productService.deleteProduct(VALID_ID));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteProduct_whenProductIdInvalid_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.deleteProduct(INVALID_ID));
        verify(productRepository, never()).findById(any(ObjectId.class));
    }
}