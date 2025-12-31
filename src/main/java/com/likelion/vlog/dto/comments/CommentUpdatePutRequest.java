package com.likelion.vlog.dto.comments;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PUT /api/v1/posts/{postId}/comments/{commentId} 요청 객체
 */
@Getter
@NoArgsConstructor
public class CommentUpdatePutRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
