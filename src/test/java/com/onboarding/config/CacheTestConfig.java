package com.onboarding.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;


@TestConfiguration
@ActiveProfiles("junit")
@EnableCaching
public class CacheTestConfig {

    @Value("${cache.TTL}")
    private int duration;
    @Value("${cache.max-size}")
    private int maxSize;
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("accountInvoices");
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(duration))
                        .maximumSize(maxSize)
        );
        return cacheManager;
    }
}
