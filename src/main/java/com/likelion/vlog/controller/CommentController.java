package com.likelion.vlog.controller;

import com.likelion.vlog.dto.comments.*;
import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글/대댓글 API 컨트롤러
 * - Base URL: /api/v1/posts/{postId}/comments
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 목록 조회 (GET /api/v1/posts/{postId}/comments)
     * - 대댓글 포함
     * - 인증 불필요
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentWithRepliesGetResponse>>> getComments(
            @PathVariable Long postId) {

        List<CommentWithRepliesGetResponse> response = commentService.getComments(postId);
        return ResponseEntity.ok(ApiResponse.success("댓글 목록 조회 성공", response));
    }

    /**
     * 댓글 작성 (POST /api/v1/posts/{postId}/comments)
     * - 인증 필요
     * - 성공 시 201 Created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentPostResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentPostResponse response = commentService.createComment(postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글 작성 성공", response));
    }

    /**
     * 댓글 수정 (PUT /api/v1/posts/{postId}/comments/{commentId})
     * - 인증 필요
     * - 작성자만 수정 가능
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentPutResponse>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdatePutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentPutResponse response = commentService.updateComment(postId, commentId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("댓글 수정 성공", response));
    }

    /**
     * 댓글 삭제 (DELETE /api/v1/posts/{postId}/comments/{commentId})
     * - 인증 필요
     * - 작성자만 삭제 가능
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<?>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(postId, commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공"));
    }

    /**
     * 답글 작성 (POST /api/v1/posts/{postId}/comments/{commentId}/replies)
     * - 인증 필요
     * - 성공 시 201 Created
     */
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<ReplyPostResponse>> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody ReplyCreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReplyPostResponse response = commentService.createReply(postId, commentId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답글 작성 성공", response));
    }

    /**
     * 답글 수정 (PUT /api/v1/posts/{postId}/comments/{commentId}/replies/{replyId})
     * - 인증 필요
     * - 작성자만 수정 가능
     */
    @PutMapping("/{commentId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<ReplyPutResponse>> updateReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @Valid @RequestBody ReplyUpdatePutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReplyPutResponse response = commentService.updateReply(postId, commentId, replyId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("답글 수정 성공", response));
    }

    /**
     * 답글 삭제 (DELETE /api/v1/posts/{postId}/comments/{commentId}/replies/{replyId})
     * - 인증 필요
     * - 작성자만 삭제 가능
     */
    @DeleteMapping("/{commentId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<?>> deleteReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteReply(postId, commentId, replyId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("답글 삭제 성공"));
    }
}
