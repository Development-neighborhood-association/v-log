package com.likelion.vlog.dto.like;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class LikeResponse {
    private Long likeCount;
    private Boolean checkLike;
}
