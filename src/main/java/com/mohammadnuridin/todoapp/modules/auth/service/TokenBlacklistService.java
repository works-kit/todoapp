package com.mohammadnuridin.todoapp.modules.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Masukkan token ke blacklist Redis dengan TTL sesuai sisa expire token.
     * Setelah token expired secara alami, Redis otomatis hapus key-nya.
     *
     * @param token      JWT access token yang akan di-blacklist
     * @param ttlSeconds sisa waktu hidup token dalam detik
     */
    public void blacklist(String token, long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + token, "deleted", ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Cek apakah token ada di blacklist.
     *
     * @param token JWT token yang akan dicek
     * @return true jika token sudah di-blacklist (logout)
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
