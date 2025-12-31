package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * PUT /api/v1/posts/{postId}/comments/{commentId}/replies/{replyId} 응답 객체
 */
@Getter
@Builder
public class ReplyPutResponse {

    private Long replyId;
    private String content;
    private AuthorResponse author;
    private Long parentCommentId;
    private LocalDateTime updatedAt;

    public static ReplyPutResponse from(Comment reply) {
        return ReplyPutResponse.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .author(AuthorResponse.from(reply.getUser()))
                .parentCommentId(reply.getParent().getId())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
