package com.likelion.vlog.dto.follows;

import com.likelion.vlog.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 팔로워 목록 조회
 * GET api/v1/users/{userId}/followers 요청 객체
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowerGetResponse {
    private Long userId;
    private String nickname;
    private boolean isFollowing;

    public static FollowerGetResponse of(User user, boolean isFollowing) {
        return FollowerGetResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .isFollowing(isFollowing)
                .build();
    }
}
