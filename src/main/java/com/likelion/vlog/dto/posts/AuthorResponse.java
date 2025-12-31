package com.likelion.vlog.dto.posts;

import com.likelion.vlog.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthorResponse {
    private Long userId;
    private String nickname;

    public static AuthorResponse from(User user) {
        return AuthorResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
