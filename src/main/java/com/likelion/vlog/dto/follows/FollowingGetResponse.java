package com.likelion.vlog.dto.follows;

import com.likelion.vlog.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 팔로잉 목록 조회
 * Get api/v1/users/{userId}/followings 요청 객체
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowingGetResponse {
    private Long userId;
    private String nickname;
    private boolean isFollowing;

    public static FollowingGetResponse of(User user, boolean isFollowing) {
        return FollowingGetResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .isFollowing(isFollowing)
                .build();
    }
}
