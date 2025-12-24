package com.likelion.vlog.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 공통 DTO
 * - 제네릭으로 다양한 목록에 재사용 가능
 * - Spring Data Page 객체를 API 응답 형식으로 변환
 */
@Getter
@Builder
public class PageResponse<T> {
    private List<T> content;        // 실제 데이터 목록
    private PageInfo pageInfo;      // 페이징 메타 정보

    @Getter
    @Builder
    public static class PageInfo {
        private int page;           // 현재 페이지 번호 (0부터 시작)
        private int size;           // 페이지당 개수
        private long totalElements; // 전체 데이터 개수
        private int totalPages;     // 전체 페이지 수
        private boolean first;      // 첫 페이지 여부
        private boolean last;       // 마지막 페이지 여부
    }

    /**
     * Spring Data Page를 API 응답 형식으로 변환
     * @param page Spring Data Page 객체 (페이징 정보 추출용)
     * @param content 변환된 DTO 목록
     */
    public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .pageInfo(PageInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .build())
                .build();
    }
}