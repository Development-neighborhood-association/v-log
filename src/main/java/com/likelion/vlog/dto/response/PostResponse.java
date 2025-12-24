package com.likelion.vlog.dto.response;

import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 응답 DTO
 * - 게시글 전체 내용 + 댓글 목록 포함
 */
@Getter
@Builder
public class PostResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private List<String> tags;
    private int likeCount;
    private boolean isLiked;      // 현재 로그인한 사용자가 좋아요 했는지 여부
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 상세 조회용 - 좋아요, 댓글 포함
     */
    public static PostResponse of(Post post, List<String> tags, int likeCount, boolean isLiked, List<CommentResponse> comments) {
        return PostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .tags(tags)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .comments(comments)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 작성/수정 응답용 - 좋아요, 댓글 제외
     */
    public static PostResponse of(Post post, List<String> tags) {
        return PostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .tags(tags)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}