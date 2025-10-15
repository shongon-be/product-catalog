package com.shongon.catalog.unit;

import com.shongon.catalog.controller.ProductController;
import com.shongon.catalog.dto.cache.CacheablePage;
import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.*;
import com.shongon.catalog.enums.SortField;
import com.shongon.catalog.exception.ErrorCode;
import com.shongon.catalog.exception.ProductCatalogException;
import com.shongon.catalog.service.ICacheService;
import com.shongon.catalog.service.IProductService;
import com.shongon.catalog.service.ISortFilterService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private IProductService productService;

    @Mock
    private ISortFilterService sortFilterService;

    @Mock
    private ICacheService cacheService;

    private final String VALID_ID = "68aae2cfcb79c11df8cda5ed";
    private final String INVALID_ID = "123";
    private ViewAllProductsResponse product1, product2;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        product1 = createViewAllProduct("68aae2cfcb79c11df8cda5ed", "Test Food Product 1", "Test Food Description 1", 10.0, "FOOD");
        product2 = createViewAllProduct("68aae2cfce79c11df8cda5ed", "Test Food Product 2", "Test Food Description 2", 10.0, "FOOD");

        createRequest = createProductRequest("Test Product", "Description", 10.0, "FOOD");
        updateRequest = createUpdateRequest("Updated Product", "Updated Description", 120.0, "FOOD");
    }

    // Helper methods
    private ViewAllProductsResponse createViewAllProduct(String id, String name, String desc, Double price, String category) {
        ViewAllProductsResponse product = new ViewAllProductsResponse();
        product.setId(id);
        product.setName(name);
        product.setDescription(desc);
        product.setPrice(price);
        product.setCategory(category);
        return product;
    }

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

    // GET ALL PRODUCTS TESTS
    @Test
    void getAllProducts_validRequest_returnsProducts() {
        Page<ViewAllProductsResponse> productPage = new PageImpl<>(List.of(product1, product2));
        when(productService.viewAllProducts(any())).thenReturn(productPage);

        ApiResponse<Page<ViewAllProductsResponse>> response = productController.getAllProducts(PageRequest.of(0, 20));

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(2, response.getResult().getContent().size());
        verify(productService).viewAllProducts(any());
    }

    @Test
    void getAllProducts_validRequest_returnEmpty() {
        when(productService.viewAllProducts(any())).thenReturn(Page.empty());

        ApiResponse<Page<ViewAllProductsResponse>> response = productController.getAllProducts(PageRequest.of(0, 20));

        assertEquals(200, response.getCode());
        assertTrue(response.getResult().isEmpty());
        verify(productService).viewAllProducts(any());
    }

    // GET BY ID TESTS
    @Test
    void getProductById_validRequest_returnsProduct() {
        GetProductResponse product = new GetProductResponse();
        product.setId(VALID_ID);
        product.setName("Test Food Product 1");
        when(productService.getProductById(VALID_ID)).thenReturn(product);

        ApiResponse<GetProductResponse> response = productController.getProductById(VALID_ID);

        assertEquals(200, response.getCode());
        assertEquals(VALID_ID, response.getResult().getId());
        verify(productService).getProductById(VALID_ID);
    }

    @Test
    void getProductById_invalidId_throwsException() {
        when(productService.getProductById(INVALID_ID))
                .thenThrow(new IllegalArgumentException("Invalid product id"));

        assertThrows(IllegalArgumentException.class,
                () -> productController.getProductById(INVALID_ID));
        verify(productService).getProductById(INVALID_ID);
    }

    @Test
    void getProductById_productNotFound_throwException() {
        when(productService.getProductById(anyString()))
                .thenThrow(new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));

        assertThrows(ProductCatalogException.class,
                () -> productController.getProductById(VALID_ID));
        verify(productService).getProductById(VALID_ID);
    }

    // CREATE PRODUCT TESTS
    @Test
    void createProduct_validRequest() {
        CreateProductResponse response = new CreateProductResponse();
        response.setMessage("Create product successfully!");
        when(productService.createProduct(createRequest)).thenReturn(response);

        ApiResponse<CreateProductResponse> result = productController.createProduct(createRequest);

        assertEquals(201, result.getCode());
        assertEquals("Create product successfully!", result.getResult().getMessage());
        verify(productService).createProduct(createRequest);
    }

    // Test validation errors for create
    @Test
    void createProduct_blankName_throwsException() {
        createRequest.setName("");
        when(productService.createProduct(createRequest))
                .thenThrow(new ConstraintViolationException("Product name cannot be blank", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(createRequest));
    }

    @Test
    void createProduct_duplicateName_throwsException() {
        when(productService.createProduct(createRequest))
                .thenThrow(new ProductCatalogException(ErrorCode.PRODUCT_ALREADY_EXISTS));

        assertThrows(ProductCatalogException.class,
                () -> productController.createProduct(createRequest));
    }

    @Test
    void createProduct_tooShortName_throwsException() {
        createRequest.setName("a");
        testCreateValidationException("Product name must be between 2 and 100 characters");
    }

    @Test
    void createProduct_tooLongName_throwsException() {
        createRequest.setName("A".repeat(101));
        testCreateValidationException("Product name must be between 2 and 100 characters");
    }

    @Test
    void createProduct_blankDescription_throwsException() {
        createRequest.setDescription("");
        testCreateValidationException("Product description cannot be blank");
    }

    @Test
    void createProduct_tooLongDescription_throwsException() {
        createRequest.setDescription("A".repeat(501));
        testCreateValidationException("Description cannot exceed 500 characters");
    }

    @Test
    void createProduct_nullPrice_throwsException() {
        createRequest.setPrice(null);
        testCreateValidationException("Product price cannot be null");
    }

    @Test
    void createProduct_nagativePrice_throwsException() {
        createRequest.setPrice(-100.0);
        testCreateValidationException("Price must be greater than 0");
    }

    @Test
    void createProduct_zeroPrice_throwsException() {
        createRequest.setPrice(0.0);
        testCreateValidationException("Price must be greater than 0");
    }

    @Test
    void createProduct_exceedMaxPrice_throwsException() {
        createRequest.setPrice(1000000.0);
        testCreateValidationException("Price cannot exceed $999,999.99");
    }

    @Test
    void createProduct_blankCategory_throwsException() {
        createRequest.setCategory("");
        testCreateValidationException("Product category cannot be blank");
    }

    @Test
    void createProduct_invalidCategory_throwsException() {
        createRequest.setCategory("TOY");
        testCreateValidationException("Category must be one of: ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM");
    }

    // Helper method for create validation tests
    private void testCreateValidationException(String errorMessage) {
        when(productService.createProduct(createRequest))
                .thenThrow(new ConstraintViolationException(errorMessage, null));
        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(createRequest));
    }

    // UPDATE PRODUCT TESTS
    @Test
    void updateProduct_validRequest() {
        UpdateProductResponse response = new UpdateProductResponse();
        response.setMessage("Update product successfully!");
        when(productService.updateProduct(VALID_ID, updateRequest)).thenReturn(response);

        ApiResponse<UpdateProductResponse> result = productController.updateProduct(VALID_ID, updateRequest);

        assertEquals(200, result.getCode());
        assertEquals("Update product successfully!", result.getResult().getMessage());
        verify(productService).updateProduct(VALID_ID, updateRequest);
    }

    @Test
    void updateProduct_productNotFound_throwsException() {
        when(productService.updateProduct(anyString(), any()))
                .thenThrow(new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));

        assertThrows(ProductCatalogException.class,
                () -> productController.updateProduct(VALID_ID, updateRequest));
    }

    @Test
    void updateProduct_invalidId_throwsException() {
        when(productService.updateProduct(INVALID_ID, updateRequest))
                .thenThrow(new IllegalArgumentException("Invalid product id"));

        assertThrows(IllegalArgumentException.class,
                () -> productController.updateProduct(INVALID_ID, updateRequest));
    }

    // Test validation errors for update (similar to create)
    @Test
    void updateProduct_tooShortName_throwsException() {
        updateRequest.setName("a");
        testUpdateValidationException("Product name must be between 2 and 100 characters");
    }

    @Test
    void updateProduct_tooLongName_throwsException() {
        updateRequest.setName("A".repeat(101));
        testUpdateValidationException("Product name must be between 2 and 100 characters");
    }

    @Test
    void updateProduct_blankDescription_throwsException() {
        updateRequest.setDescription("");
        testUpdateValidationException("Product description cannot be blank");
    }

    @Test
    void updateProduct_tooLongDescription_throwsException() {
        updateRequest.setDescription("A".repeat(501));
        testUpdateValidationException("Description cannot exceed 500 characters");
    }

    @Test
    void updateProduct_nullPrice_throwsException() {
        updateRequest.setPrice(null);
        testUpdateValidationException("Product price cannot be null");
    }

    @Test
    void updateProduct_nagativePrice_throwsException() {
        updateRequest.setPrice(-100.0);
        testUpdateValidationException("Price must be greater than 0");
    }

    @Test
    void updateProduct_zeroPrice_throwsException() {
        updateRequest.setPrice(0.0);
        testUpdateValidationException("Price must be greater than 0");
    }

    @Test
    void updateProduct_exceedMaxPrice_throwsException() {
        updateRequest.setPrice(1000000.0);
        testUpdateValidationException("Price cannot exceed $999,999.99");
    }

    @Test
    void updateProduct_blankCategory_throwsException() {
        updateRequest.setCategory("");
        testUpdateValidationException("Product category cannot be blank");
    }

    @Test
    void updateProduct_invalidCategory_throwsException() {
        updateRequest.setCategory("TOY");
        testUpdateValidationException("Category must be one of: ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM");
    }

    // Helper method for update validation tests
    private void testUpdateValidationException(String errorMessage) {
        when(productService.updateProduct(VALID_ID, updateRequest))
                .thenThrow(new ConstraintViolationException(errorMessage, null));
        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(VALID_ID, updateRequest));
    }

    // DELETE PRODUCT TESTS
    @Test
    void deleteProduct_validRequest() {
        doNothing().when(productService).deleteProduct(VALID_ID);

        ApiResponse<Void> response = productController.deleteProduct(VALID_ID);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        verify(productService).deleteProduct(VALID_ID);
    }

    @Test
    void deleteProduct_invalidId_throwsException() {
        doThrow(new IllegalArgumentException("Invalid product id"))
                .when(productService).deleteProduct(INVALID_ID);

        assertThrows(IllegalArgumentException.class,
                () -> productController.deleteProduct(INVALID_ID));
        verify(productService).deleteProduct(INVALID_ID);
    }

    @Test
    void deleteProduct_productNotFound_throwsException() {
        doThrow(new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND))
                .when(productService).deleteProduct(anyString());

        assertThrows(ProductCatalogException.class,
                () -> productController.deleteProduct(VALID_ID));
        verify(productService).deleteProduct(VALID_ID);
    }

    // FILTER PRODUCTS (service only, ignore cache because tested separately)
    @Test
    void filterProductsByCategory_shouldReturnsResult() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ViewAllProductsResponse> page =
                new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(sortFilterService.filterAndSortProducts(eq("FOOD"), isNull(), isNull(), eq(pageable)))
                .thenReturn(page);

        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.filterProductsByCategory("FOOD", pageable);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(2, response.getResult().getContent().size());

        verify(sortFilterService).filterAndSortProducts(eq("FOOD"), isNull(), isNull(), eq(pageable));
    }

    // SORT PRODUCTS
    @Test
    void filterAndSortProduct_shouldReturnsResult() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ViewAllProductsResponse> page =
                new PageImpl<>(List.of(product1), pageable, 1);

        when(sortFilterService.filterAndSortProducts(eq("FOOD"),
                eq(SortField.PRICE),
                eq(Sort.Direction.DESC),
                eq(pageable)))
                .thenReturn(page);

        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.filterAndSortProduct("FOOD", SortField.PRICE, Sort.Direction.DESC, pageable);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(1, response.getResult().getContent().size());

        verify(sortFilterService).filterAndSortProducts(eq("FOOD"),
                eq(SortField.PRICE),
                eq(Sort.Direction.DESC),
                eq(pageable));
    }

    // CACHE TESTS
    @Test
    void getAllProducts_cacheHit_returnsCachedData() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ViewAllProductsResponse> page =
                new PageImpl<>(List.of(product1), pageable, 1);

        CacheablePage<ViewAllProductsResponse> cachedPage = CacheablePage.from(page);

        when(cacheService.generateCacheKey(any(), any(), any(), any(), any()))
                .thenReturn("cache-key");
        when(cacheService.getFromCache(eq("cache-key"), any())).thenReturn(cachedPage);

        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.getAllProducts(pageable);

        assertEquals(200, response.getCode());
        assertEquals("Success (Cached)", response.getMessage());
        assertEquals(1, response.getResult().getContent().size());

        // Không gọi DB service khi cache hit
        verify(productService, never()).viewAllProducts(any());
    }

    @Test
    void getAllProducts_cacheMiss_fetchesFromDbAndSaves() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ViewAllProductsResponse> page =
                new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(cacheService.generateCacheKey(any(), any(), any(), any(), any()))
                .thenReturn("cache-key");
        when(cacheService.getFromCache(eq("cache-key"), any())).thenReturn(null);
        when(productService.viewAllProducts(pageable)).thenReturn(page);

        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.getAllProducts(pageable);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(2, response.getResult().getContent().size());

        verify(productService).viewAllProducts(pageable);
        verify(cacheService).saveToCache(eq("cache-key"), any(CacheablePage.class), any());
    }

    @Test
    void filterProductsByCategory_cacheHit_returnsCachedData() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ViewAllProductsResponse> page =
                new PageImpl<>(List.of(product1), pageable, 1);

        CacheablePage<ViewAllProductsResponse> cachedPage = CacheablePage.from(page);

        // doReturn thay cho when để tránh Strict stubbing varargs
        doReturn("filter-key")
                .when(cacheService)
                .generateCacheKey(any(), any(), any(), any(), any(), any());
        when(cacheService.getFromCache(eq("filter-key"), any())).thenReturn(cachedPage);

        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.filterProductsByCategory("FOOD", pageable);

        assertEquals(200, response.getCode());
        assertEquals("Success (Cached)", response.getMessage());
        assertEquals(1, response.getResult().getContent().size());

        verify(sortFilterService, never()).filterAndSortProducts(any(), any(), any(), any());
    }

    @Test
    void filterProductsByCategory_cacheMiss_fetchesFromDbAndSaves() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ViewAllProductsResponse> page =
                new PageImpl<>(List.of(product1, product2), pageable, 2);

        // doReturn thay cho when
        doReturn("filter-key")
                .when(cacheService)
                .generateCacheKey(any(), any(), any(), any(), any(), any());
        when(cacheService.getFromCache(eq("filter-key"), any())).thenReturn(null);
        when(sortFilterService.filterAndSortProducts(eq("FOOD"), isNull(), isNull(), eq(pageable)))
                .thenReturn(page);

        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.filterProductsByCategory("FOOD", pageable);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(2, response.getResult().getContent().size());

        verify(sortFilterService).filterAndSortProducts(eq("FOOD"), isNull(), isNull(), eq(pageable));
        verify(cacheService).saveToCache(eq("filter-key"), any(CacheablePage.class), any());
    }

    @Test
    void createProduct_shouldEvictCache() {
        CreateProductResponse createResp = new CreateProductResponse();
        createResp.setMessage("Created!");
        when(productService.createProduct(createRequest)).thenReturn(createResp);
        when(cacheService.generateCacheKey("*")).thenReturn("pattern");

        ApiResponse<CreateProductResponse> response = productController.createProduct(createRequest);

        assertEquals(201, response.getCode());
        verify(cacheService).evictCacheByPattern("pattern");
    }

    @Test
    void updateProduct_shouldEvictCache() {
        UpdateProductResponse updateResp = new UpdateProductResponse();
        updateResp.setMessage("Updated!");
        when(productService.updateProduct(VALID_ID, updateRequest)).thenReturn(updateResp);
        when(cacheService.generateCacheKey("*")).thenReturn("pattern");

        ApiResponse<UpdateProductResponse> response =
                productController.updateProduct(VALID_ID, updateRequest);

        assertEquals(200, response.getCode());
        verify(cacheService).evictCacheByPattern("pattern");
    }

    @Test
    void deleteProduct_shouldEvictCache() {
        doNothing().when(productService).deleteProduct(VALID_ID);
        when(cacheService.generateCacheKey("*")).thenReturn("pattern");

        ApiResponse<Void> response = productController.deleteProduct(VALID_ID);

        assertEquals(200, response.getCode());
        verify(cacheService).evictCacheByPattern("pattern");
    }

}