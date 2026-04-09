package com.jinju.jinjuwiki.domain.user.service;

import com.jinju.jinjuwiki.domain.user.dto.request.UserNicknameUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.request.UserPasswordUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.response.UserNicknameUpdateResponse;
import com.jinju.jinjuwiki.domain.user.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    UserNicknameUpdateResponse updateNickname(Long userId, UserNicknameUpdateRequest request);

    void updatePassword(Long userId, UserPasswordUpdateRequest request);
}
