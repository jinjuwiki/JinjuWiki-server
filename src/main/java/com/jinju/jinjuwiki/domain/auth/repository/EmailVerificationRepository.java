package com.jinju.jinjuwiki.domain.auth.repository;

import com.jinju.jinjuwiki.domain.auth.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    void deleteByEmail(String email);
}
