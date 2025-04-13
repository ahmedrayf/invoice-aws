package com.onboarding.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;


@TestConfiguration
@ActiveProfiles("junit")
@EnableCaching
public class CacheTestConfig {


    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("accountInvoices");
    }
}
