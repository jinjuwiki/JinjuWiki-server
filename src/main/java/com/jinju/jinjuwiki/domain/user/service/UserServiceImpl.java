package com.jinju.jinjuwiki.domain.user.service;

import com.jinju.jinjuwiki.domain.user.dto.request.UserNicknameUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.request.UserPasswordUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.response.UserNicknameUpdateResponse;
import com.jinju.jinjuwiki.domain.user.dto.response.UserProfileResponse;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public UserNicknameUpdateResponse updateNickname(Long userId, UserNicknameUpdateRequest request) {
        User user = getUser(userId);

        if (!user.getNickname().equals(request.nickname()) && userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateNickname(request.nickname());
        return new UserNicknameUpdateResponse(user.getId(), user.getNickname());
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UserPasswordUpdateRequest request) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
