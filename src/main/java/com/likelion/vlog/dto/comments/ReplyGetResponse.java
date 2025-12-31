package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * GET /posts/{postId}/comments 내 replies 배열용 응답 객체
 */
@Getter
@Builder
public class ReplyGetResponse {

    private Long replyId;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReplyGetResponse from(Comment reply) {
        return ReplyGetResponse.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .author(AuthorResponse.from(reply.getUser()))
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
