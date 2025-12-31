package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * POST /api/v1/posts/{postId}/comments/{commentId}/replies 응답 객체
 */
@Getter
@Builder
public class ReplyPostResponse {

    private Long replyId;
    private String content;
    private AuthorResponse author;
    private Long parentCommentId;
    private LocalDateTime createdAt;

    public static ReplyPostResponse from(Comment reply) {
        return ReplyPostResponse.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .author(AuthorResponse.from(reply.getUser()))
                .parentCommentId(reply.getParent().getId())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
