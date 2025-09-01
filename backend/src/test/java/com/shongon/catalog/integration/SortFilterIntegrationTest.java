package com.shongon.catalog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("Sort/Filter Product Integration Tests")
public class SortFilterIntegrationTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SORT_FILTER_URL = "/products/filter";


    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        List<Product> products = List.of(
                createProduct("Book A", 10.0, "BOOKS"),
                createProduct("Book B", 20.0, "BOOKS"),
                createProduct("Book C", 15.0, "BOOKS"),
                createProduct("Food A", 5.0, "FOOD")
        );
        productRepository.saveAll(products);
    }

    private Product createProduct(String name, double price, String category) {
        return Product.builder()
                .id(new ObjectId())
                .name(name)
                .description("Description " + name)
                .price(price)
                .category(category)
                .build();
    }

    @Test
    @DisplayName("Should filter by category and sort DESC by price")
    void whenCategoryAndSortDesc_returnSortedPage() throws Exception {
        mockMvc.perform(get(SORT_FILTER_URL)
                        .param("category", "BOOKS")
                        .param("field", "PRICE")
                        .param("direction", "DESC")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalElements").value(3))
                .andExpect(jsonPath("$.result.content[0].price").value(20.0))
                .andExpect(jsonPath("$.result.content[1].price").value(15.0))
                .andExpect(jsonPath("$.result.content[2].price").value(10.0));
    }

    @Test
    @DisplayName("Should sort ASC by price when direction=ASC")
    void whenSortAsc_returnSortedAsc() throws Exception {
        mockMvc.perform(get(SORT_FILTER_URL)
                        .param("category", "BOOKS")
                        .param("field", "PRICE")
                        .param("direction", "ASC")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalElements").value(3))
                .andExpect(jsonPath("$.result.content[0].price").value(10.0))
                .andExpect(jsonPath("$.result.content[1].price").value(15.0))
                .andExpect(jsonPath("$.result.content[2].price").value(20.0));
    }

    @Test
    @DisplayName("Should paginate results correctly")
    void whenPaginationApplied_returnPagedResults() throws Exception {
        // Page 0, size 2
        mockMvc.perform(get(SORT_FILTER_URL)
                        .param("category", "BOOKS")
                        .param("field", "PRICE")
                        .param("direction", "DESC")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.length()").value(2))
                .andExpect(jsonPath("$.result.totalElements").value(3))
                .andExpect(jsonPath("$.result.content[0].price").value(20.0));

        // Page 1, size 2 -> chỉ còn 1 phần tử
        mockMvc.perform(get(SORT_FILTER_URL)
                        .param("category", "BOOKS")
                        .param("field", "PRICE")
                        .param("direction", "DESC")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.length()").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(3))
                .andExpect(jsonPath("$.result.content[0].price").value(10.0));
    }

    @Test
    @DisplayName("Should return all categories when category param is missing")
    void whenNoCategoryProvided_returnAll() throws Exception {
        mockMvc.perform(get(SORT_FILTER_URL)
                        .param("field", "PRICE")
                        .param("direction", "ASC")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalElements").value(4));
    }

    @Test
    @DisplayName("Should return 400 when invalid sort field provided")
    void whenInvalidSortField_return400() throws Exception {
        mockMvc.perform(get(SORT_FILTER_URL)
                        .param("category", "BOOKS")
                        .param("field", "WRONG")
                        .param("direction", "ASC")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errors[0]").value("Parameter 'field' has invalid value 'WRONG'"));
    }
}
