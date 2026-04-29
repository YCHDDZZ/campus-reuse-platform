package com.campus.reuse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisCacheErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheErrorHandler.class);

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get failed, fallback to database. cache={}, key={}, message={}", cache == null ? "unknown" : cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put failed, ignore cache write. cache={}, key={}, message={}", cache == null ? "unknown" : cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict failed, ignore cache evict. cache={}, key={}, message={}", cache == null ? "unknown" : cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear failed, ignore cache clear. cache={}, message={}", cache == null ? "unknown" : cache.getName(), exception.getMessage());
            }
        };
    }
}
