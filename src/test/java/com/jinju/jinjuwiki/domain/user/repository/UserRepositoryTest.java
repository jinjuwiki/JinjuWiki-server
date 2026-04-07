package com.jinju.jinjuwiki.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("DB unique 제약으로 이메일이 중복되면 데이터 무결성 예외가 발생한다.")
    void duplicateEmailCanFailAtDatabaseLevel() {
        userRepository.save(User.builder()
                .email("dbdup@test.com")
                .password("encoded-password")
                .nickname("dbUser1")
                .role(UserRole.USER)
                .build());

        assertThatThrownBy(() -> userRepository.saveAndFlush(User.builder()
                .email("dbdup@test.com")
                .password("encoded-password")
                .nickname("dbUser2")
                .role(UserRole.USER)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
