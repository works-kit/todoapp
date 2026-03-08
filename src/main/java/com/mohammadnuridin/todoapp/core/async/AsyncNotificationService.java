package com.mohammadnuridin.todoapp.core.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Contoh penggunaan @Async untuk operasi non-blocking.
 *
 * Semua method di sini dijalankan di thread pool "taskExecutor"
 * (dikonfigurasi di AsyncConfig) — tidak memblokir HTTP request thread.
 *
 * Cocok untuk:
 * - Audit logging ke storage eksternal
 * - Kirim email/notifikasi setelah operasi
 * - Sinkronisasi data ke sistem lain
 *
 * PENTING — dua aturan @Async:
 * 1. Method harus dipanggil dari bean LAIN (bukan dari class yang sama)
 * karena Spring proxy tidak intercept self-invocation.
 * 2. Method harus public.
 */
@Slf4j
@Service
public class AsyncNotificationService {

    /**
     * Kirim notifikasi setelah register — fire and forget.
     * Caller tidak perlu tunggu email terkirim.
     *
     * Dipanggil dari AuthServiceImpl setelah userRepository.save().
     */
    @Async
    public void sendWelcomeNotification(String email, String name) {
        log.info("[ASYNC] Sending welcome notification to: {}", email);

        // Simulasi kirim email / push notification
        // emailService.sendWelcome(email, name);
        // pushService.sendWelcome(name);

        log.info("[ASYNC] Welcome notification sent to: {}", email);
    }

    /**
     * Log audit event ke storage eksternal — fire and forget.
     * Tidak memblokir response ke client.
     */
    @Async
    public void logAuditEvent(String userId, String action, String detail) {
        log.info("[ASYNC] AUDIT userId={} action={} detail={}", userId, action, detail);

        // Contoh: simpan ke audit table atau kirim ke ELK
        // auditRepository.save(new AuditLog(userId, action, detail));
    }

    /**
     * Contoh @Async yang return CompletableFuture.
     * Caller bisa .thenApply() atau .exceptionally() untuk handle result.
     *
     * Gunakan ini jika caller butuh hasil dari operasi async.
     */
    @Async
    public CompletableFuture<Boolean> checkEmailBlacklist(String email) {
        log.debug("[ASYNC] Checking email blacklist: {}", email);

        // Simulasi call ke external service
        boolean isBlacklisted = false; // externalService.isBlacklisted(email);

        return CompletableFuture.completedFuture(isBlacklisted);
    }
}