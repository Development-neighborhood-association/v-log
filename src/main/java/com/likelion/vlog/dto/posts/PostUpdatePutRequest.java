package com.likelion.vlog.dto.posts;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PUT /api/v1/posts/{postId} 요청 객체
 */
@Getter
@NoArgsConstructor
public class PostUpdatePutRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> tags;
}
