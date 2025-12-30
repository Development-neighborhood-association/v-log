package com.likelion.vlog.service;

import com.likelion.vlog.dto.like.LikeResponse;
import com.likelion.vlog.entity.Like;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.repository.LikeRepository;
import com.likelion.vlog.repository.PostRepository;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 좋아요 추가
    public LikeResponse addLike(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 중복 체크
        if (likeRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            throw new IllegalStateException("이미 좋아요를 누른 게시글입니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Like like = Like.from(user, post);
        likeRepository.save(like);

        Long count = likeRepository.countByPostId(postId);
        return new LikeResponse(count, true);
    }

    // 좋아요 삭제
    public LikeResponse removeLike(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Like like = likeRepository.findByUserIdAndPostId(user.getId(), postId)
                .orElseThrow(() -> new IllegalStateException("좋아요를 찾을 수 없습니다."));

        likeRepository.delete(like);

        Long count = likeRepository.countByPostId(postId);
        return new LikeResponse(count, false);
    }

    // 좋아요 정보 조회
    @Transactional(readOnly = true)
    public LikeResponse getLikeInfo(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long count = likeRepository.countByPostId(postId);
        boolean checkLike = likeRepository.existsByUserIdAndPostId(user.getId(), postId);
        return new LikeResponse(count, checkLike);
    }
}