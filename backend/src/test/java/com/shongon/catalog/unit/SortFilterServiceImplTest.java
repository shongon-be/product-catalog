package com.shongon.catalog.unit;

import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.enums.SortField;
import com.shongon.catalog.mapper.ProductMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.service.impl.SortFilterServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SortFilterServiceImplTest {

    @InjectMocks
    SortFilterServiceImpl filterSortService;

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    ProductMapper productMapper;

    @Test
    void filterAndSortProducts_baseCase_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);

        List<Product> mockProducts = List.of(product1, product2);

        // Mock mongoTemplate.find() -> return list of products
        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);

        // Mock mongoTemplate.count() -> return total number of products
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(5L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);


        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl(null, null,null, 1, 2);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(5); // total product = 3
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void filterAndSortProducts_withCategory_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product1, product2, product3);

        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl("BOOKS", null,null, 0, 3);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res1, res2, res3);
    }

    @Test
    void filterAndSortProducts_sortByNameASC_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product1, product2, product3);

        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl(null, SortField.NAME, Sort.Direction.ASC, 0, 3);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query builtQuery = queryCaptor.getValue();

        // Then
        assertThat(builtQuery.getSortObject().toJson())
                .contains("\"name\": 1"); // ASC
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res1, res2, res3);
    }

    @Test
    void filterAndSortProducts_sortByNameDESC_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product3, product2, product1);

        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl(null, SortField.NAME, Sort.Direction.DESC, 0, 3);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query builtQuery = queryCaptor.getValue();

        // Then
        assertThat(builtQuery.getSortObject().toJson())
                .contains("\"name\": -1"); // DESC
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res3, res2, res1);
    }

    @Test
    void filterAndSortProducts_sortByPriceASC_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product1, product2, product3);

        // Mock mongoTemplate.find() -> return list of products
        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);

        // Mock mongoTemplate.count() -> return total number of products
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl(null, SortField.PRICE, Sort.Direction.ASC, 0, 3);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query builtQuery = queryCaptor.getValue();

        // Then
        assertThat(builtQuery.getSortObject().toJson())
                .contains("\"price\": 1"); // DESC
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res1, res2, res3);
    }

    @Test
    void filterAndSortProducts_sortByPriceDESC_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product3, product2, product1);

        // Mock mongoTemplate.find() -> return list of products
        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);

        // Mock mongoTemplate.count() -> return total number of products
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl(null, SortField.PRICE, Sort.Direction.DESC, 0, 3);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query builtQuery = queryCaptor.getValue();

        // Then
        assertThat(builtQuery.getSortObject().toJson())
                .contains("\"price\": -1"); // DESC
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res3, res2, res1);
    }

    @Test
    void filterAndSortProducts_withCategoryAndPriceASC_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product1, product2, product3);

        // Mock mongoTemplate.find() -> return list of products
        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);

        // Mock mongoTemplate.count() -> return total number of products
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl("BOOKS",SortField.PRICE, Sort.Direction.ASC, 0, 3);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query builtQuery = queryCaptor.getValue();

        // Then
        assertThat(builtQuery.getSortObject().toJson())
                .contains("\"price\": 1"); // ASC
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res1, res2, res3);

        verify(mongoTemplate, times(1))
                .find(any(Query.class), eq(Product.class));
    }

    @Test
    void filterAndSortProducts_withCategoryAndPriceDESC_returnsPageOfProducts() {
        // Given
        Product product1 = createSampleProduct("Test Product 1", "Test Description 1", 10.0);
        Product product2 = createSampleProduct("Test Product 2", "Test Description 2", 12.0);
        Product product3 = createSampleProduct("Test Product 3", "Test Description 3", 15.0);

        List<Product> mockProducts = List.of(product3, product2, product1);

        // Mock mongoTemplate.find() -> return list of products
        when(mongoTemplate.find(any(Query.class), eq(Product.class)))
                .thenReturn(mockProducts);

        // Mock mongoTemplate.count() -> return total number of products
        when(mongoTemplate.count(any(Query.class), eq(Product.class)))
                .thenReturn(30L);

        // Response DTO
        ViewAllProductsResponse res1 = viewAllProductsResponse(product1);
        ViewAllProductsResponse res2 = viewAllProductsResponse(product2);
        ViewAllProductsResponse res3 = viewAllProductsResponse(product3);

        // Mock mapper
        convertToMapper(product1, res1);
        convertToMapper(product2, res2);
        convertToMapper(product3, res3);

        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl("BOOKS",SortField.PRICE, Sort.Direction.DESC, 0, 3);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class));
        Query builtQuery = queryCaptor.getValue();

        // Then
        assertThat(builtQuery.getSortObject().toJson())
                .contains("\"price\": -1"); // DESC
        assertThat(result.getTotalElements()).isEqualTo(30); // total suitable product = 30
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(res3, res2, res1);

        verify(mongoTemplate, times(1))
                .find(any(Query.class), eq(Product.class));
    }

    @Test
    void filterAndSortProducts_withInvalidCategory_returnsEmptyPage() {
        // When
        Page<ViewAllProductsResponse> result = mockServiceImpl("INVALID", null,null, 0, 3);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(0); // total suitable product = 0
        assertThat(result.getContent()).isEmpty();
    }

    // HELPER METHODS
    private Page<ViewAllProductsResponse> mockServiceImpl(String category, SortField sortBy, Sort.Direction direction, int page, int size) {
        return filterSortService.filterAndSortProducts(
                category,
                sortBy,
                direction,
                PageRequest.of(page, size)
        );
    }

    private void convertToMapper(Product product1, ViewAllProductsResponse res1) {
        when(productMapper.toViewAllProductsResponse(product1)).thenReturn(res1);
    }

    private Product createSampleProduct(String name, String description, double price) {
        return Product.builder()
                .id(new ObjectId())
                .name(name)
                .description(description)
                .price(price)
                .category("BOOKS")
                .build();
    }

    private ViewAllProductsResponse viewAllProductsResponse(Product product) {
        ViewAllProductsResponse response = new ViewAllProductsResponse();
        response.setId(product.getId().toString());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setCategory(product.getCategory());

        return response;
    }
}
