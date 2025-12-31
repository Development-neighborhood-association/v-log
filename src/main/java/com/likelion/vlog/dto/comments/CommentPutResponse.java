package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * PUT /api/v1/posts/{postId}/comments/{commentId} 응답 객체
 */
@Getter
@Builder
public class CommentPutResponse {

    private Long commentId;
    private String content;
    private AuthorResponse author;
    private LocalDateTime updatedAt;

    public static CommentPutResponse from(Comment comment) {
        return CommentPutResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .author(AuthorResponse.from(comment.getUser()))
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
