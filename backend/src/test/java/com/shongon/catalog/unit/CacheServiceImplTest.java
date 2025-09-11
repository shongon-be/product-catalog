package com.shongon.catalog.unit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shongon.catalog.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceImplTest {

    @InjectMocks
    private CacheServiceImpl cacheService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    void setup() {
        // inject private field cachePrefix
        ReflectionTestUtils.setField(cacheService, "cachePrefix", "product-catalog-test:");
    }

    @Test
    void testGetFromCache_Hit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("product-catalog-test:key")).thenReturn("cached");
        when(objectMapper.convertValue(eq("cached"), any(TypeReference.class)))
                .thenReturn("cached");

        String result = cacheService.getFromCache("product-catalog-test:key",
                new TypeReference<String>() {});
        assertThat(result).isEqualTo("cached");

        verify(valueOps).get("product-catalog-test:key");
        verify(objectMapper).convertValue(eq("cached"), any(TypeReference.class));
    }

    @Test
    void testGetFromCache_Miss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("product-catalog-test:key")).thenReturn(null);

        String result = cacheService.getFromCache("product-catalog-test:key",
                new TypeReference<String>() {});
        assertThat(result).isNull();

        verify(valueOps).get("product-catalog-test:key");
        verifyNoInteractions(objectMapper);
    }

    @Test
    void testSaveToCache() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        cacheService.saveToCache("product-catalog-test:key", "data",
                Duration.ofMinutes(5));

        verify(valueOps).set("product-catalog-test:key", "data",
                Duration.ofMinutes(5));
    }

    @Test
    void testEvictCacheByPattern() {
        when(redisTemplate.keys("product-catalog-test:*"))
                .thenReturn(Set.of("product-catalog-test:1", "product-catalog-test:2"));

        cacheService.evictCacheByPattern("product-catalog-test:*");

        verify(redisTemplate).delete(Set.of("product-catalog-test:1", "product-catalog-test:2"));
    }

    @Test
    void testGenerateCacheKey() {
        String key = cacheService.generateCacheKey("all", "page", 1, "size", 10);
        assertThat(key).isEqualTo("product-catalog-test:all:page:1:size:10");
    }
}
