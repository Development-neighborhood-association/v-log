package com.likelion.vlog.dto.response;

import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 목록 조회 응답 DTO
 * - 목록에서는 content 대신 summary(100자 요약) 사용
 * - 댓글 목록 대신 commentCount만 포함
 */
@Getter
@Builder
public class PostListResponse {
    private Long postId;
    private String title;
    private String summary;         // content의 앞 100자 + "..."
    private AuthorResponse author;
    private List<String> tags;
    private int likeCount;
    private int commentCount;       // 댓글 개수만 표시
    private LocalDateTime createdAt;

    public static PostListResponse of(Post post, List<String> tags, int likeCount, int commentCount) {
        // content를 100자로 잘라서 summary 생성
        String summary = post.getContent();
        if (summary != null && summary.length() > 100) {
            summary = summary.substring(0, 100) + "...";
        }

        return PostListResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .summary(summary)
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .tags(tags)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}