package com.shongon.catalog.controller;

import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.dto.response.*;
import com.shongon.catalog.exception.ErrorCode;
import com.shongon.catalog.exception.ProductCatalogException;
import com.shongon.catalog.service.IProductService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enable mockito for mocking dependencies
public class ProductControllerTest {

    @InjectMocks
    private ProductController productController; // class under test

    @Mock
    private IProductService productService; // mock dependency of class under test

    // Get all products
    @Test
    void getAllProducts_validRequest_returnsProducts() {
        // Given
            // Create test products
        ViewAllProductsResponse product1 = new ViewAllProductsResponse();
            product1.setId("68aae2cfcb79c11df8cda5ed");
            product1.setName("Test Food Product 1");
            product1.setDescription("Test Food Description 1");
            product1.setPrice(10.0);
            product1.setCategory("FOOD");
        ViewAllProductsResponse product2 = new ViewAllProductsResponse();
            product2.setId("68aae2cfce79c11df8cda5ed");
            product2.setName("Test Food Product 2");
            product2.setDescription("Test Food Description 2");
            product2.setPrice(10.0);
            product2.setCategory("FOOD");

            // Convert to Page
        Page<ViewAllProductsResponse> productPage = new PageImpl<>(List.of(product1, product2));

            // Mock service to return test products
        Mockito.when(productService.viewAllProducts(Mockito.any()))
                .thenReturn(productPage);

        // When
        ApiResponse<Page<ViewAllProductsResponse>> response = productController
                .getAllProducts(PageRequest.of(0, 20));

        // Then
        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(2, response.getResult().getContent().size());
        assertEquals(product1, response.getResult().getContent().get(0));
        assertEquals(product2, response.getResult().getContent().get(1));

            // Verify service was called
        Mockito.verify(productService,
                times(1)).viewAllProducts(Mockito.any()
        );
    }

    @Test
    void getAllProducts_validRequest_returnEmpty() {
        // Given
            // Mock service to return an empty page
        Mockito.when(productService.viewAllProducts(Mockito.any()))
                .thenReturn(Page.empty());

        // When
        ApiResponse<Page<ViewAllProductsResponse>> response =
                productController.getAllProducts(PageRequest.of(0, 20));
        // Then
        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(0, response.getResult().getContent().size());
        assertTrue(response.getResult().isEmpty());

        Mockito.verify(productService,
                times(1)).viewAllProducts(Mockito.any()
        );
    }

    // Get by id
    @Test
    void getProductById_validRequest_returnsProduct() {
        String productId = "68ab02d98321e47a8bae053a";
        GetProductResponse product = new GetProductResponse();
            product.setId(productId);
            product.setName("Test Food Product 1");
            product.setDescription("Test Food Description 1");
            product.setPrice(10.0);
            product.setCategory("FOOD");

        when(productService.getProductById(productId)).thenReturn(product);

        ApiResponse<GetProductResponse> response = productController.getProductById(productId);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals(productId, response.getResult().getId());
        assertEquals("Test Food Product 1", response.getResult().getName());
        assertEquals("Test Food Description 1", response.getResult().getDescription());
        assertEquals(10.0, response.getResult().getPrice());
        assertEquals("FOOD", response.getResult().getCategory());

        Mockito.verify(productService, times(1))
                .getProductById(productId);
    }

    @Test
    void getProductById_invalidId_throwsException() {
        String productId = "123";

        when(productService.getProductById(productId))
                .thenThrow(new IllegalArgumentException("Invalid product id"));

        assertThrows(IllegalArgumentException.class,
                () -> productController.getProductById(productId));

        Mockito.verify(productService, times(1))
                .getProductById(productId);
    }

    @Test
    void getProductById_productNotFound_throwException() {
        String productId = "68ab02d98321e47a8bae053m";

        when(productService.getProductById(productId))
                .thenThrow(new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));

        assertThrows(ProductCatalogException.class,
                () -> productController.getProductById(productId));

        Mockito.verify(productService, times(1))
                .getProductById(productId);
    }

    // Create
    @Test
    void createProduct_validRequest() {
        CreateProductRequest product = new CreateProductRequest();
            product.setName("Test Food Product 1");
            product.setDescription("Test Food Description 1");
            product.setPrice(10.0);
            product.setCategory("FOOD");
        CreateProductResponse createdResponse = new CreateProductResponse();
            createdResponse.setMessage("Create product successfully!");

        when(productService.createProduct(product))
                .thenReturn(createdResponse);

        ApiResponse<CreateProductResponse> response = productController.createProduct(product);

        assertEquals(201, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals("Create product successfully!", response.getResult().getMessage());

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_blankName_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
            product.setName("");
            product.setDescription("Test Food Description 1");
            product.setPrice(10.0);
            product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Product name cannot be blank", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_duplicateName_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ProductCatalogException(ErrorCode.PRODUCT_ALREADY_EXISTS));

        assertThrows(ProductCatalogException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_tooShortName_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("a");
        product.setDescription("Test Food Description 1");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Product name must be between 2 and 100 characters", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_tooLongName_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("A comprehensive nine-step process for planning coding projects covers everything from defining project goals and target users to selecting tech stacks and deployment strategies. The methodology emphasizes starting with clear objectives, writing user stories, designing data models, ruthlessly scoping an MVP, creating simple wireframes, considering future scalability, choosing appropriate architecture, selecting the right technology stack, and following a structured development process.");
        product.setDescription("Test Food Description 1");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Product name must be between 2 and 100 characters", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_blankDescription_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Product description cannot be blank", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_tooLongDescription_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("A comprehensive nine-step process for planning coding projects covers everything from defining project goals and target users to selecting tech stacks and deployment strategies. The methodology emphasizes starting with clear objectives, writing user stories, designing data models, ruthlessly scoping an MVP, creating simple wireframes, considering future scalability, choosing appropriate architecture, selecting the right technology stack, and following a structured development process.");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Description cannot exceed 500 characters", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_nullPrice_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(null);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Product price cannot be null", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_nagativePrice_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(-100.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Price must be greater than 0", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_zeroPrice_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(0.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Price must be greater than 0", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_exceedMaxPrice_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(1000000.0);
        product.setCategory("FOOD");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Price cannot exceed $999,999.99", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void createProduct_blankCategory_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(1.0);
        product.setCategory("");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Product category cannot be blank", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    void createProduct_invalidCategory_throwsException() {
        CreateProductRequest product = new CreateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(1.0);
        product.setCategory("TOY");

        when(productService.createProduct(product))
                .thenThrow(new ConstraintViolationException("Category must be one of: ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.createProduct(product));

        verify(productService, times(1)).createProduct(product);
    }

    // Update
    @Test
    void updateProduct_validRequest() {
        String productId = "68ab02d98321e47a8bae053a";
        UpdateProductRequest product = new UpdateProductRequest();
            product.setName("Test Food Product 2");
            product.setDescription("Test Food Description 2");
            product.setPrice(120.0);
            product.setCategory("FOOD");
        UpdateProductResponse updatedResponse = new UpdateProductResponse();
            updatedResponse.setMessage("Update product successfully!");

        when(productService.updateProduct(productId,product))
                .thenReturn(updatedResponse);

        ApiResponse<UpdateProductResponse> response = productController.updateProduct(productId,product);

        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals("Update product successfully!", response.getResult().getMessage());

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_productNotFound_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
            product.setName("Test Food Product 2");
            product.setDescription("Test Food Description 2");
            product.setPrice(120.0);
            product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND));

        assertThrows(ProductCatalogException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_invalidId_throwsException() {
        String productId = "123";
        UpdateProductRequest product = new UpdateProductRequest();
            product.setName("Test Food Product 2");
            product.setDescription("Test Food Description 2");
            product.setPrice(120.0);
            product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new IllegalArgumentException("Invalid product id"));

        assertThrows(IllegalArgumentException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_tooShortName_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
            product.setName("a");
            product.setDescription("Test Food Description 1");
            product.setPrice(10.0);
            product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Product name must be between 2 and 100 characters", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_tooLongName_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("A comprehensive nine-step process for planning coding projects covers everything from defining project goals and target users to selecting tech stacks and deployment strategies. The methodology emphasizes starting with clear objectives, writing user stories, designing data models, ruthlessly scoping an MVP, creating simple wireframes, considering future scalability, choosing appropriate architecture, selecting the right technology stack, and following a structured development process.");
        product.setDescription("Test Food Description 1");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Product name must be between 2 and 100 characters", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_blankDescription_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Product description cannot be blank", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_tooLongDescription_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("A comprehensive nine-step process for planning coding projects covers everything from defining project goals and target users to selecting tech stacks and deployment strategies. The methodology emphasizes starting with clear objectives, writing user stories, designing data models, ruthlessly scoping an MVP, creating simple wireframes, considering future scalability, choosing appropriate architecture, selecting the right technology stack, and following a structured development process.");
        product.setPrice(10.0);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Description cannot exceed 500 characters", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_nullPrice_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(null);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Product price cannot be null", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_nagativePrice_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(-100.0);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Price must be greater than 0", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_zeroPrice_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(0.0);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Price must be greater than 0", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_exceedMaxPrice_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(1000000.0);
        product.setCategory("FOOD");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Price cannot exceed $999,999.99", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    @Test
    void updateProduct_blankCategory_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(1.0);
        product.setCategory("");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Product category cannot be blank", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    void updateProduct_invalidCategory_throwsException() {
        String productId = "68ab02d98321e47a8bae053d";
        UpdateProductRequest product = new UpdateProductRequest();
        product.setName("Test Food Product 1");
        product.setDescription("Test Food Description 1");
        product.setPrice(1.0);
        product.setCategory("TOY");

        when(productService.updateProduct(productId,product))
                .thenThrow(new ConstraintViolationException("Category must be one of: ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM", null));

        assertThrows(ConstraintViolationException.class,
                () -> productController.updateProduct(productId,product));

        verify(productService, times(1)).updateProduct(productId,product);
    }

    // Delete
    @Test
    void deleteProduct_validRequest() {
        String productId = "68ab02d98321e47a8bae053a";

        doNothing().when(productService).deleteProduct(productId);

        ApiResponse<Void> response = productController.deleteProduct(productId);

        assertEquals(204, response.getCode());
        assertEquals("Success", response.getMessage());

        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    void deleteProduct_invalidId_throwsException() {
        String productId = "123";

        doThrow(new IllegalArgumentException("Invalid product id"))
                .when(productService).deleteProduct(productId);

        assertThrows(IllegalArgumentException.class,
                () -> productController.deleteProduct(productId));

        Mockito.verify(productService, times(1))
                .deleteProduct(productId);
    }

    @Test
    void deleteProduct_productNotFound_throwsException() {
        String productId = "123";

        doThrow(new ProductCatalogException(ErrorCode.PRODUCT_NOT_FOUND))
                .when(productService).deleteProduct(productId);

        assertThrows(ProductCatalogException.class,
                () -> productController.deleteProduct(productId));

        Mockito.verify(productService, times(1))
                .deleteProduct(productId);
    }
}
