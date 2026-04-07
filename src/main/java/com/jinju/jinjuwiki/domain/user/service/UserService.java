package com.jinju.jinjuwiki.domain.user.service;

import com.jinju.jinjuwiki.domain.user.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);
}
