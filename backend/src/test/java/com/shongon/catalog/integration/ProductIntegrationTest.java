package com.shongon.catalog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shongon.catalog.dto.request.CreateProductRequest;
import com.shongon.catalog.dto.request.UpdateProductRequest;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("Product Integration Tests")
public class ProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constants - tránh lặp lại magic strings
    private static final String PRODUCTS_URL = "/products";
    private static final String PRODUCT_BY_ID_URL = "/products/{productId}";
    private static final String VALID_PRODUCT_ID = "68ad8b8f1f76bd5e1eb753cd";
    private static final String NOT_FOUND_PRODUCT_ID = "68aaee0ad86372261d4b5e4e";
    private static final String INVALID_PRODUCT_ID = "66aaa1111111111111111";

    @BeforeEach
    void setUp() throws InterruptedException {
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
        Thread.sleep(500); // chờ Redis ổn định (nếu container vừa lên)

        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            // Retry 1 lần sau 300ms nếu Redis chưa kịp ready
            Thread.sleep(300);
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }
        productRepository.deleteAll();
    }

    @AfterAll
    static void cleanUp(@Autowired RedisTemplate<String, Object> redisTemplate) {
        redisTemplate.getConnectionFactory().getConnection().close();
    }

    // Helper methods - tránh duplicate code
    private Product createSampleProduct(String id, String name) {
        return Product.builder()
                .id(new ObjectId(id))
                .name(name)
                .description("Sample Description")
                .price(10.0)
                .category("FOOD")
                .build();
    }

    private CreateProductRequest createValidProductRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Food Product");
        request.setDescription("New Food Description");
        request.setPrice(10.0);
        request.setCategory("FOOD");
        return request;
    }

    private UpdateProductRequest createValidUpdateRequest() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Update Food Product");
        request.setDescription("Update Food Description");
        request.setPrice(100.0);
        request.setCategory("FOOD");
        return request;
    }

    @Nested
    @DisplayName("View All Products")
    class ViewAllProductsTests {

        @Test
        @DisplayName("Should return paged results when products exist")
        void whenProductsExist_returnPagedResult() throws Exception {
            // Given
            List<Product> products = List.of(
                    createSampleProduct(VALID_PRODUCT_ID, "Sample Food Product 1"),
                    createSampleProduct("68ad8b8f1f76bd5e1eb753ce", "Sample Food Product 2")
            );
            productRepository.saveAll(products);

            // When & Then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.totalElements").value(2))
                    .andExpect(jsonPath("$.result.content[0].name").value("Sample Food Product 1"));
        }

        @Test
        @DisplayName("Should return empty page when no products exist")
        void whenNoProductsExist_returnEmptyPage() throws Exception {
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "0")
                            .param("size", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("Get Product By ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when valid ID provided")
        void whenValidId_returnProduct() throws Exception {
            // Given
            Product product = createSampleProduct(VALID_PRODUCT_ID, "Sample Food Product");
            productRepository.save(product);

            // When & Then
            mockMvc.perform(get(PRODUCT_BY_ID_URL, VALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.name").value("Sample Food Product"));
        }

        @Test
        @DisplayName("Should return 400 when invalid ID format")
        void whenInvalidIdFormat_return400() throws Exception {
            mockMvc.perform(get(PRODUCT_BY_ID_URL, INVALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.errors").value("Invalid productId format. Must be a valid Mongo ObjectId"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void whenProductNotFound_return404() throws Exception {
            mockMvc.perform(get(PRODUCT_BY_ID_URL, NOT_FOUND_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.errors").value("Product not found"));
        }
    }

    @Nested
    @DisplayName("Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully with valid data")
        void whenValidData_createSuccessfully() throws Exception {
            CreateProductRequest request = createValidProductRequest();

            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.result.message").value("Create product successfully!"));
        }

        @Test
        @DisplayName("Should return 409 when product name already exists")
        void whenDuplicateName_return409() throws Exception {
            // Given
            Product existingProduct = createSampleProduct(VALID_PRODUCT_ID, "Existing Product");
            productRepository.save(existingProduct);

            CreateProductRequest request = createValidProductRequest();
            request.setName("Existing Product");

            // When & Then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.errors").value("Product already exists"));
        }

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {

            @Test
            @DisplayName("Should return 400 when name is blank")
            void whenBlankName_return400() throws Exception {
                CreateProductRequest request = createValidProductRequest();
                request.setName("");

                mockMvc.perform(post(PRODUCTS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value(400))
                        .andExpect(jsonPath("$.errors[*]").value(
                                containsInAnyOrder(
                                        "Product name cannot be blank",
                                        "Product name must be between 2 and 100 characters"
                                )
                        ));
            }

            @Test
            @DisplayName("Should return 400 when name is too short")
            void whenNameTooShort_return400() throws Exception {
                CreateProductRequest request = createValidProductRequest();
                request.setName("A");

                mockMvc.perform(post(PRODUCTS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors").value("Product name must be between 2 and 100 characters"));
            }

            @Test
            @DisplayName("Should return 400 when price is null")
            void whenPriceNull_return400() throws Exception {
                CreateProductRequest request = createValidProductRequest();
                request.setPrice(null);

                mockMvc.perform(post(PRODUCTS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors").value("Product price cannot be null"));
            }

            @Test
            @DisplayName("Should return 400 when price is zero or negative")
            void whenInvalidPrice_return400() throws Exception {
                CreateProductRequest request = createValidProductRequest();
                request.setPrice(0.0);

                mockMvc.perform(post(PRODUCTS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors").value("Price must be greater than 0"));
            }

            @Test
            @DisplayName("Should return 400 when category is invalid")
            void whenInvalidCategory_return400() throws Exception {
                CreateProductRequest request = createValidProductRequest();
                request.setCategory("INVALID");

                mockMvc.perform(post(PRODUCTS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors").value("Category must be one of: ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM"));
            }
        }
    }

    @Nested
    @DisplayName("Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully with valid data")
        void whenValidData_updateSuccessfully() throws Exception {
            // Given
            Product existingProduct = createSampleProduct(VALID_PRODUCT_ID, "Original Product");
            productRepository.save(existingProduct);

            UpdateProductRequest request = createValidUpdateRequest();

            // When & Then
            mockMvc.perform(put(PRODUCT_BY_ID_URL, VALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.message").value("Update product successfully!"));
        }

        @Test
        @DisplayName("Should return 400 when product ID is invalid")
        void whenInvalidProductId_return400() throws Exception {
            UpdateProductRequest request = createValidUpdateRequest();

            mockMvc.perform(put(PRODUCT_BY_ID_URL, INVALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").value("Invalid productId format. Must be a valid Mongo ObjectId"));
        }

        @Test
        @DisplayName("Should return 409 when updating product with duplicate name")
        void whenDuplicateName_return409() throws Exception {
            Product existingProduct = createSampleProduct(VALID_PRODUCT_ID, "Original Product");
            Product savedProduct = createSampleProduct(new ObjectId().toString(), "Sample Product");
            productRepository.save(existingProduct);
            productRepository.save(savedProduct);

            UpdateProductRequest request = new UpdateProductRequest();
            request.setName("Sample Product");
            request.setDescription("Sample Description");
            request.setPrice(10.0);
            request.setCategory("FOOD");

            mockMvc.perform(put(PRODUCTS_URL + "/" + existingProduct.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.errors[0]").value("Product already exists"));
        }
    }

    @Nested
    @DisplayName("Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void whenValidRequest_deleteSuccessfully() throws Exception {
            // Given
            Product product = createSampleProduct(VALID_PRODUCT_ID, "Product to Delete");
            productRepository.save(product);

            // When & Then
            mockMvc.perform(delete(PRODUCT_BY_ID_URL, VALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"));
        }

        @Test
        @DisplayName("Should return 400 when product ID is invalid")
        void whenInvalidProductId_return400() throws Exception {
            mockMvc.perform(delete(PRODUCT_BY_ID_URL, INVALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").value("Invalid productId format. Must be a valid Mongo ObjectId"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void whenProductNotFound_return404() throws Exception {
            mockMvc.perform(delete(PRODUCT_BY_ID_URL, NOT_FOUND_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").value("Product not found"));
        }
    }

    @Nested
    @DisplayName("Cache Tests")
    class CacheTests {

        @Test
        @DisplayName("Should return cached result on second GET all products request (Cache HIT)")
        void givenProductsInDb_whenGetAllTwice_thenSecondTimeUsesCache() throws Exception {
            // Given
            Product product = createSampleProduct(VALID_PRODUCT_ID, "Cached Product");
            productRepository.save(product);

            // First request -> MISS, fetch DB
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Success"));

            // Second request -> HIT, fetch cache
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Success (Cached)"))
                    .andExpect(jsonPath("$.result.content[0].name").value("Cached Product"));
        }

        @Test
        @DisplayName("Should evict cache after CREATE product")
        void givenCacheExists_whenCreateProduct_thenCacheEvicted() throws Exception {
            // Prepare cache by calling GET
            Product product = createSampleProduct(VALID_PRODUCT_ID, "Before Create");
            productRepository.save(product);

            mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Success"));

            // Create new product -> should evict cache
            CreateProductRequest request = createValidProductRequest();
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // GET again -> should MISS and reload from DB
            mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Success"));
        }

        @Test
        @DisplayName("Should evict cache after UPDATE product")
        void givenCacheExists_whenUpdateProduct_thenCacheEvicted() throws Exception {
            Product product = createSampleProduct(VALID_PRODUCT_ID, "Old Name");
            productRepository.save(product);

            // Warm up cache
            mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk());

            // Update product
            UpdateProductRequest updateReq = createValidUpdateRequest();
            mockMvc.perform(put(PRODUCT_BY_ID_URL, VALID_PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk());

            // GET again -> MISS and reload
            mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Success"));
        }

        @Test
        @DisplayName("Should evict cache after DELETE product")
        void givenCacheExists_whenDeleteProduct_thenCacheEvicted() throws Exception {
            Product product = createSampleProduct(VALID_PRODUCT_ID, "To Delete");
            productRepository.save(product);

            // Warm up cache
            mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk());

            // Delete product
            mockMvc.perform(delete(PRODUCT_BY_ID_URL, VALID_PRODUCT_ID))
                    .andExpect(status().isOk());

            // GET again -> MISS
            mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Success"));
        }
    }
}