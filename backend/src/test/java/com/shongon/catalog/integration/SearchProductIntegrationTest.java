package com.shongon.catalog.integration;

import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("Search Product Integration Tests")
public class SearchProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    private static final String SEARCH_URL = "/products/search";

    // Helper method to create products for testing
    private Product createProduct(String name, String category) {
        return Product.builder()
                .id(new ObjectId())
                .name(name)
                .description("Description for " + name)
                .price(Math.random() * 100)
                .category(category)
                .build();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        // Ensure Redis connection is stable and flush all data
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
        Thread.sleep(500); // Wait for Redis to be stable
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            Thread.sleep(300); // Retry if Redis was not ready
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }

        // Clean up MongoDB and seed with test data
        productRepository.deleteAll();
        List<Product> products = List.of(
                createProduct("Apple iPhone 15 Pro", "ELECTRONICS"),
                createProduct("Apple MacBook Pro 16-inch", "ELECTRONICS"),
                createProduct("Samsung Galaxy S24 Ultra", "ELECTRONICS"),
                createProduct("Sony WH-1000XM5 Headphones", "ELECTRONICS"),
                createProduct("A Brief History of Time", "BOOKS")
        );
        productRepository.saveAll(products);
    }

    @AfterAll
    static void cleanUp(@Autowired RedisTemplate<String, Object> redisTemplate) {
        // Close Redis connection after all tests
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().close();
        }
    }

    @Test
    @DisplayName("Should return products matching partial keyword")
    void whenSearchWithPartialKeyword_thenReturnMatchingProducts() throws Exception {
        mockMvc.perform(get(SEARCH_URL)
                        .param("keyword", "Pro")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.content[*].name",
                        containsInAnyOrder("Apple iPhone 15 Pro", "Apple MacBook Pro 16-inch")));
    }

    @Test
    @DisplayName("Should return products matching case-insensitive keyword")
    void whenSearchWithCaseInsensitiveKeyword_thenReturnMatchingProducts() throws Exception {
        mockMvc.perform(get(SEARCH_URL)
                        .param("keyword", "apple") // Lowercase search
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.content[*].name",
                        containsInAnyOrder("Apple iPhone 15 Pro", "Apple MacBook Pro 16-inch")));
    }

    @Test
    @DisplayName("Should return empty page when no products match keyword")
    void whenSearchWithNonMatchingKeyword_thenReturnEmptyPage() throws Exception {
        mockMvc.perform(get(SEARCH_URL)
                        .param("keyword", "NonExistentProduct")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(0))
                .andExpect(jsonPath("$.result.content").isEmpty());
    }

    @Test
    @DisplayName("Should return all products when keyword is empty")
    void whenSearchWithEmptyKeyword_thenReturnAllProducts() throws Exception {
        // Based on the SearchServiceImpl logic, an empty keyword should trigger findAll.
        mockMvc.perform(get(SEARCH_URL)
                        .param("keyword", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(5));
    }

    @Test
    @DisplayName("Should return all products when keyword is null (not provided)")
    void whenSearchWithNullKeyword_thenReturnAllProducts() throws Exception {
        // The controller will pass null to the service if the parameter is missing.
        mockMvc.perform(get(SEARCH_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(5));
    }

    @Test
    @DisplayName("Should paginate search results correctly")
    void whenSearchWithPagination_thenReturnPagedResults() throws Exception {
        // Page 0, Size 1 -> Should get 1 out of 2 "Apple" products
        mockMvc.perform(get(SEARCH_URL)
                        .param("keyword", "Apple")
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.content.length()").value(1));

        // Page 1, Size 1 -> Should get the second "Apple" product
        mockMvc.perform(get(SEARCH_URL)
                        .param("keyword", "Apple")
                        .param("page", "1")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.content.length()").value(1));
    }
}