package com.mohammadnuridin.todoapp.core.ratelimiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Konfigurasi Redis untuk rate limiter.
 *
 * Menggunakan StringRedisTemplate (bukan RedisTemplate<Object, Object>)
 * karena Lua script hanya butuh operasi String (GET, SET, DECR).
 *
 * Bean ini tidak konflik dengan RedisTemplate lain yang mungkin ada
 * di aplikasi (misal untuk cache session) karena pakai @Bean berbeda.
 *
 * Kelas ini menggantikan RateLimiterCacheConfig (Caffeine) yang dihapus.
 * Dependency Caffeine & caffeine-guava tidak lagi dibutuhkan untuk rate limiter.
 */
@Configuration
public class RateLimiterConfig {

    /**
     * StringRedisTemplate khusus rate limiter.
     *
     * Menggunakan StringRedisSerializer eksplisit agar key Redis terbaca
     * manusia di Redis CLI / monitoring tool:
     *   rate:global:192.168.1.1  ✅
     *   \xac\xed\x00\x05t...     ❌ (default Java serialization)
     */
    @Bean("rateLimiterRedisTemplate")
    public StringRedisTemplate rateLimiterRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
