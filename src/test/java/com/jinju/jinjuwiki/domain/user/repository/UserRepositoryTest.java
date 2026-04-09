package com.jinju.jinjuwiki.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

// 유저 저장소 JPA 슬라이스 테스트 클래스
@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    private final UserRepository userRepository;

    @Autowired
    UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("DB unique 제약으로 이메일이 중복되면 데이터 무결성 예외가 발생한다.")
    void duplicateEmailCanFailAtDatabaseLevel() {
        // given
        userRepository.save(User.builder()
                .email("dbdup@test.com")
                .password("encoded-password")
                .nickname("dbUser1")
                .role(UserRole.USER)
                .build());

        // when
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(User.builder()
                .email("dbdup@test.com")
                .password("encoded-password")
                .nickname("dbUser2")
                .role(UserRole.USER)
                .build())
        );

        // then
        assertThat(exception).isInstanceOf(DataIntegrityViolationException.class);
    }
}
