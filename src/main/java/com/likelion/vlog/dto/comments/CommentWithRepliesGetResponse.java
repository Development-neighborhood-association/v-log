package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GET /posts/{postId}/comments 응답 객체 (대댓글 포함)
 */
@Getter
@Builder
public class CommentWithRepliesGetResponse {

    private Long commentId;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReplyGetResponse> replies;

    public static CommentWithRepliesGetResponse from(Comment comment) {
        return CommentWithRepliesGetResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .author(AuthorResponse.from(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(comment.getChildren().stream()
                        .map(ReplyGetResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
