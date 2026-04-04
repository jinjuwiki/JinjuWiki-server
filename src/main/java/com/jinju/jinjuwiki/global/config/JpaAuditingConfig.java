package com.jinju.jinjuwiki.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA Auditing을 켜주는 클래스 (EnableJpaAuditing -> BaseEntity)
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
