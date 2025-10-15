package com.shongon.catalog.unit;

import com.shongon.catalog.dto.response.ViewAllProductsResponse;
import com.shongon.catalog.mapper.ProductMapper;
import com.shongon.catalog.model.Product;
import com.shongon.catalog.repository.ProductRepository;
import com.shongon.catalog.service.impl.SearchServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchProductImplTest {
    @InjectMocks
    private SearchServiceImpl searchService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Test
    void whenSearchWithKeyword_thenReturnsMatchingProducts() {
        // Arrange (Given) - Chuẩn bị dữ liệu và định nghĩa hành vi của mock
        String keyword = "Laptop";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        // Tạo dữ liệu giả
        Product product = new Product(); // Giả sử Product có constructor rỗng hoặc dùng builder
        product.setName("Laptop Gaming");

        ViewAllProductsResponse responseDto = new ViewAllProductsResponse(); // Tương tự cho DTO

        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        // Định nghĩa hành vi cho mock:
        // 1. Khi repository.searchProducts được gọi với keyword và pageable này...
        when(productRepository.searchProducts(keyword, pageable)).thenReturn(productPage);
        // 2. Khi mapper.toViewAllProductsResponse được gọi với product này...
        when(productMapper.toViewAllProductsResponse(product)).thenReturn(responseDto);

        // Act (When) - Gọi phương thức cần test
        Page<ViewAllProductsResponse> result = searchService.searchProducts(keyword, page, size, null, null);

        // Assert (Then) - Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // Kiểm tra số lượng phần tử
        assertEquals(responseDto, result.getContent().get(0)); // Kiểm tra nội dung

        // Verify: Xác minh rằng các phương thức mock đã được gọi đúng
        verify(productRepository).searchProducts(keyword, pageable); // Đảm bảo searchProducts được gọi
        verify(productRepository, never()).findAll(any(Pageable.class)); // Đảm bảo findAll không bao giờ được gọi
        verify(productMapper).toViewAllProductsResponse(product); // Đảm bảo mapper được gọi
    }

    @Test
    void whenSearchWithNullKeyword_thenReturnsAllProducts() {
        // Arrange (Given)
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Product product = new Product();
        product.setName("Any Product");
        ViewAllProductsResponse responseDto = new ViewAllProductsResponse();

        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        // Định nghĩa hành vi cho mock repository khi keyword là null
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toViewAllProductsResponse(any(Product.class))).thenReturn(responseDto);

        // Act (When)
        Page<ViewAllProductsResponse> result = searchService.searchProducts(null, page, size, null, null);

        // Assert (Then)
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify: Quan trọng nhất là xác minh phương thức nào được gọi
        verify(productRepository).findAll(pageable); // Đảm bảo findAll được gọi
        verify(productRepository, never()).searchProducts(anyString(), any(Pageable.class)); // Đảm bảo searchProducts không bao giờ được gọi
    }

    @Test
    void whenSearchWithBlankKeyword_thenReturnsAllProducts() {
        // Arrange (Given)
        String blankKeyword = "   "; // Keyword chỉ chứa khoảng trắng
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Product product = new Product();
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        // Logic tương tự như trường hợp keyword là null
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        // Không cần mock mapper nữa nếu chỉ muốn verify luồng gọi repository

        // Act (When)
        searchService.searchProducts(blankKeyword, page, size, null, null);

        // Assert (Then) - Chỉ cần verify là đủ
        verify(productRepository).findAll(pageable);
        verify(productRepository, never()).searchProducts(anyString(), any(Pageable.class));
    }
}
