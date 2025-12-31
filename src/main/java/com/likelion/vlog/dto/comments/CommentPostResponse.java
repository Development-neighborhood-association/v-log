package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * POST /api/v1/posts/{postId}/comments 응답 객체
 */
@Getter
@Builder
public class CommentPostResponse {

    private Long commentId;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createdAt;

    public static CommentPostResponse from(Comment comment) {
        return CommentPostResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .author(AuthorResponse.from(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
