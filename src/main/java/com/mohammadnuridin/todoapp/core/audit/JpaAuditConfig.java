package com.mohammadnuridin.todoapp.core.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
    // Mengaktifkan @CreatedDate dan @LastModifiedDate di BaseEntity
    // Tidak perlu AuditorAware karena tidak pakai createdBy/updatedBy
    // Kalau nanti butuh createdBy, tambahkan AuditorAwareImpl di sini
}