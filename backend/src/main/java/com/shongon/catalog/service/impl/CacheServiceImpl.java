package com.shongon.catalog.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shongon.catalog.service.ICacheService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CacheServiceImpl implements ICacheService {

    final RedisTemplate<String, Object> redisTemplate;
    final ObjectMapper objectMapper;

    @Value("${app.cache-prefix}")
    private String cachePrefix;

    @Override
    public <T> T getFromCache(String key, TypeReference<T> typeRef) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(key);
            if (cachedData != null) {
                log.debug("Cache hit for key: {}", key);
                return objectMapper.convertValue(cachedData, typeRef);
            }
            log.debug("Cache miss for key: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Error getting data from cache for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void saveToCache(String key, Object data, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, data, ttl);
            log.debug("Data cached with key: {} and TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Error saving data to cache for key: {}", key, e);
        }
    }

    @Override
    public void evictCacheByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Evicted {} cache entries with pattern: {}", keys.size(), pattern);
            } else {
                log.debug("No cache entries found for pattern: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Error evicting cache with pattern: {}", pattern, e);
        }
    }

    @Override
    public String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(cachePrefix).append(prefix);
        for (Object param : params) {
            keyBuilder.append(":").append(param != null ? param.toString() : "null");
        }
        String key = keyBuilder.toString();
        log.debug("Generated cache key: {}", key);
        return key;
    }
}