package com.shongon.catalog.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;

public interface ICacheService {
    /**
     * Get data from cache
     * @param key Cache key
     * @param typeRef Type reference for deserialization
     * @return Cached data or null if not found/error
     */
    <T> T getFromCache(String key, TypeReference<T> typeRef);

    /**
     * Save data to cache with TTL
     * @param key Cache key
     * @param data Data to cache
     * @param ttl Time to live
     */
    void saveToCache(String key, Object data, Duration ttl);

    /**
     * Evict cache entries by pattern
     * @param pattern Pattern to match (e.g., "products:*")
     */
    void evictCacheByPattern(String pattern);

    /**
     * Generate cache key with prefix
     * @param prefix Key prefix
     * @param params Parameters to append
     * @return Generated cache key
     */
    String generateCacheKey(String prefix, Object... params);
}
