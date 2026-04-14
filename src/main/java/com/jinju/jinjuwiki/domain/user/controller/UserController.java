package com.jinju.jinjuwiki.domain.user.controller;

import com.jinju.jinjuwiki.domain.user.dto.request.UserNicknameUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.request.UserPasswordUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.response.UserNicknameUpdateResponse;
import com.jinju.jinjuwiki.domain.user.dto.response.UserProfileResponse;
import com.jinju.jinjuwiki.domain.user.service.UserService;
import com.jinju.jinjuwiki.global.error.ErrorResponse;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import com.jinju.jinjuwiki.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 프로필 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 내 프로필 조회 응답 코드 명세
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserProfileResponse response = userService.getProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PatchMapping("/me/nickname")
    @Operation(
            summary = "내 닉네임 수정",
            description = "현재 로그인한 사용자의 닉네임을 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "닉네임 수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 닉네임 형식",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 닉네임",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 내 닉네임 수정 응답 코드 명세
    public ResponseEntity<ApiResponse<UserNicknameUpdateResponse>> updateMyNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserNicknameUpdateRequest request
    ) {
        UserNicknameUpdateResponse response = userService.updateNickname(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.of("닉네임이 변경되었습니다.", response));
    }

    @PatchMapping("/me/password")
    @Operation(
            summary = "내 비밀번호 수정",
            description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 비밀번호 형식 또는 현재 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 내 비밀번호 수정 응답 코드 명세
    public ResponseEntity<ApiResponse<Void>> updateMyPassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserPasswordUpdateRequest request
    ) {
        userService.updatePassword(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
    }
}
