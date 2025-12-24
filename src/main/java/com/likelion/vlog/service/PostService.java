package com.likelion.vlog.service;

import com.likelion.vlog.dto.request.PostCreateRequest;
import com.likelion.vlog.dto.request.PostUpdateRequest;
import com.likelion.vlog.dto.response.*;
import com.likelion.vlog.entity.*;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 비즈니스 로직
 * - @Transactional(readOnly = true): 기본적으로 읽기 전용 (성능 최적화)
 * - 쓰기 메서드에만 @Transactional 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TagMapRepository tagMapRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    /**
     * 게시글 목록 조회 (페이징 + 필터링)
     * - tag: 특정 태그가 달린 게시글만 조회
     * - blogId: 특정 블로그의 게시글만 조회
     * - 둘 다 null이면 전체 조회
     * - N+1 문제 해결: 좋아요/댓글 수를 벌크 쿼리로 한번에 조회
     */
    public PageResponse<PostListResponse> getPosts(String tag, Long blogId, Pageable pageable) {
        Page<Post> postPage;

        // 필터 조건에 따라 다른 쿼리 실행
        if (tag != null && blogId != null) {
            postPage = postRepository.findAllByTagNameAndBlogId(tag, blogId, pageable);
        } else if (tag != null) {
            postPage = postRepository.findAllByTagName(tag, pageable);
        } else if (blogId != null) {
            postPage = postRepository.findAllByBlogId(blogId, pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        List<Post> posts = postPage.getContent();

        // N+1 해결: 좋아요/댓글 수를 벌크 쿼리로 한번에 조회
        Map<Long, Integer> likeCountMap = getLikeCountMap(posts);
        Map<Long, Integer> commentCountMap = getCommentCountMap(posts);

        // Entity -> DTO 변환
        List<PostListResponse> content = posts.stream()
                .map(post -> {
                    List<String> tags = getTagNames(post);
                    int likeCount = likeCountMap.getOrDefault(post.getId(), 0);
                    int commentCount = commentCountMap.getOrDefault(post.getId(), 0);
                    return PostListResponse.of(post, tags, likeCount, commentCount);
                })
                .toList();

        return PageResponse.of(postPage, content);
    }

    /**
     * 여러 Post의 좋아요 수를 Map으로 변환
     */
    private Map<Long, Integer> getLikeCountMap(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        return likeRepository.countByPosts(posts).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    /**
     * 여러 Post의 댓글 수를 Map으로 변환
     */
    private Map<Long, Integer> getCommentCountMap(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        return commentRepository.countByPosts(posts).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    /**
     * 게시글 상세 조회
     * - userId가 있으면 해당 사용자의 좋아요 여부도 확인
     * - 댓글은 최상위 댓글만 조회 (대댓글은 Sprint 3에서 처리)
     */
    public PostResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));

        List<String> tags = getTagNames(post);
        int likeCount = likeRepository.countByPost(post);

        // 로그인한 사용자의 좋아요 여부 확인
        boolean isLiked = false;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                isLiked = likeRepository.existsByUserAndPost(user, post);
            }
        }

        // 최상위 댓글만 조회 (parent가 null인 댓글)
        List<CommentResponse> comments = commentRepository.findAllByPostAndParentIsNull(post).stream()
                .map(CommentResponse::from)
                .toList();

        return PostResponse.of(post, tags, likeCount, isLiked, comments);
    }

    /**
     * 게시글 작성
     * - User -> Blog 조회 후 Post 생성
     * - 태그가 있으면 자동 생성/매핑
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        Blog blog = blogRepository.findByUser(user)
                .orElseThrow(() -> NotFoundException.blog(userId));

        // Post 생성 (정적 팩토리 메서드 사용)
        Post post = Post.create(request.getTitle(), request.getContent(), blog);
        Post savedPost = postRepository.save(post);

        // 태그 저장 (없는 태그는 새로 생성)
        List<String> tagNames = saveTags(savedPost, request.getTags());

        return PostResponse.of(savedPost, tagNames);
    }

    /**
     * 게시글 수정
     * - 작성자 본인만 수정 가능 (권한 검증)
     * - 기존 태그 삭제 후 새로 저장
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));

        // 권한 검증: Post -> Blog -> User 경로로 작성자 확인
        if (!post.getBlog().getUser().getId().equals(userId)) {
            throw ForbiddenException.postUpdate();
        }

        post.update(request.getTitle(), request.getContent());

        // 태그 업데이트: 기존 매핑 삭제 후 새로 저장
        tagMapRepository.deleteAllByPost(post);
        List<String> tagNames = saveTags(post, request.getTags());

        return PostResponse.of(post, tagNames);
    }

    /**
     * 게시글 삭제
     * - 작성자 본인만 삭제 가능
     * - 태그 매핑도 함께 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));

        if (!post.getBlog().getUser().getId().equals(userId)) {
            throw ForbiddenException.postDelete();
        }

        tagMapRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    /**
     * Post의 태그 이름 목록 추출
     * - Post -> TagMap -> Tag 경로로 조회
     */
    private List<String> getTagNames(Post post) {
        return post.getTagMapList().stream()
                .map(tagMap -> tagMap.getTag().getTitle())
                .toList();
    }

    /**
     * 태그 저장 (없으면 생성)
     * - 이미 존재하는 태그면 재사용
     * - 없는 태그면 새로 생성
     * - Post-Tag 매핑(TagMap) 생성
     */
    private List<String> saveTags(Post post, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        return tagNames.stream()
                .map(tagName -> {
                    // 태그 조회 또는 생성 (정적 팩토리 메서드 사용)
                    Tag tag = tagRepository.findByTitle(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.create(tagName)));

                    // Post-Tag 매핑 생성 (정적 팩토리 메서드 사용)
                    TagMap tagMap = TagMap.create(post, tag);
                    tagMapRepository.save(tagMap);

                    return tagName;
                })
                .toList();
    }
}