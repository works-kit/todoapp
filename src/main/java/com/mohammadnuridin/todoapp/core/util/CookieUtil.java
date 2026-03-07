package com.mohammadnuridin.todoapp.core.util;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

/**
 * Utility untuk manajemen HttpOnly Cookie refresh token.
 *
 * Menggunakan addHeader() langsung — BUKAN response.addCookie() —
 * karena Java Cookie API tidak support atribut SameSite secara native.
 * Jika keduanya dipakai bersamaan akan menghasilkan dua Set-Cookie header:
 *   - Satu tanpa SameSite (dari addCookie)
 *   - Satu dengan SameSite (dari addHeader)
 * yang menyebabkan browser membaca header yang salah.
 *
 * Flag keamanan:
 *   HttpOnly      → JS tidak bisa baca token (anti-XSS)
 *   SameSite=Lax  → cookie dikirim pada same-site + navigasi top-level (anti-CSRF)
 *   Path=/api/auth → cookie hanya dikirim ke endpoint auth, tidak ke seluruh app
 *   Secure        → aktifkan di production (HTTPS) — uncomment baris Secure
 */
public final class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String COOKIE_PATH         = "/api/auth";

    private CookieUtil() {}

    /**
     * Set HttpOnly Cookie berisi refresh token.
     *
     * @param response          HttpServletResponse
     * @param token             refresh token string
     * @param maxAgeSeconds     TTL cookie dalam detik
     */
    public static void setRefreshTokenCookie(HttpServletResponse response,
                                             String token,
                                             long maxAgeSeconds) {
        response.addHeader("Set-Cookie",
                REFRESH_TOKEN_COOKIE + "=" + token
                        + "; Max-Age=" + maxAgeSeconds
                        + "; Path=" + COOKIE_PATH
                        + "; HttpOnly"
                        + "; SameSite=Lax"
                // + "; Secure"   // wajib aktifkan di production (HTTPS)
        );
    }

    /**
     * Hapus cookie refresh token saat logout.
     * Max-Age=0 instruksikan browser untuk langsung hapus cookie.
     */
    public static void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                REFRESH_TOKEN_COOKIE + "="
                        + "; Max-Age=0"
                        + "; Path=" + COOKIE_PATH
                        + "; HttpOnly"
                        + "; SameSite=Lax"
        );
    }

    /**
     * Baca refresh token dari cookie request.
     * Throw REFRESH_TOKEN_INVALID jika cookie tidak ada.
     */
    public static String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        return Arrays.stream(cookies)
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));
    }
}
