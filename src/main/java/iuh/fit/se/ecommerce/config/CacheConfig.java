package iuh.fit.se.ecommerce.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Dùng in-memory cache (ConcurrentMap)
        // Nếu có Redis, thay bằng RedisCacheManager
        return new ConcurrentMapCacheManager("nominatim");
    }
}

