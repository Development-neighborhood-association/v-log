package com.likelion.vlog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 작성 요청 DTO
 * - @Valid와 함께 사용하여 유효성 검증
 * - @NotBlank: null, "", " " 모두 불허
 */
@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    // 태그는 선택사항 (null 허용)
    private List<String> tags;
}
