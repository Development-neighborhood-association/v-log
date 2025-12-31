package com.likelion.vlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.vlog.dto.comments.*;
import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.GlobalExceptionHandler;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.service.AuthService;
import com.likelion.vlog.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private AuthService authService;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("댓글 목록 조회 API")
    class GetComments {

        @Test
        @DisplayName("댓글 목록 조회 성공")
        void getComments_Success() throws Exception {
            // given
            List<CommentWithRepliesGetResponse> responses = List.of(
                    createCommentWithRepliesGetResponse(1L, "테스트 댓글")
            );
            given(commentService.getComments(1L)).willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/v1/posts/1/comments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("댓글 목록 조회 성공"))
                    .andExpect(jsonPath("$.data[0].commentId").value(1))
                    .andExpect(jsonPath("$.data[0].content").value("테스트 댓글"));
        }
    }

    @Nested
    @DisplayName("댓글 작성 API")
    class CreateComment {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("댓글 작성 성공")
        void createComment_Success() throws Exception {
            // given
            CommentPostResponse response = createCommentPostResponse(1L, "새 댓글");
            given(commentService.createComment(eq(1L), any(CommentCreatePostRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/posts/1/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"새 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("댓글 작성 성공"))
                    .andExpect(jsonPath("$.data.commentId").value(1))
                    .andExpect(jsonPath("$.data.content").value("새 댓글"));
        }

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("존재하지 않는 게시글에 댓글 작성 시 404")
        void createComment_PostNotFound() throws Exception {
            // given
            given(commentService.createComment(eq(999L), any(CommentCreatePostRequest.class), eq("test@test.com")))
                    .willThrow(NotFoundException.post(999L));

            // when & then
            mockMvc.perform(post("/api/v1/posts/999/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"새 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("댓글 수정 API")
    class UpdateComment {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("댓글 수정 성공")
        void updateComment_Success() throws Exception {
            // given
            CommentPutResponse response = createCommentPutResponse(1L, "수정된 댓글");
            given(commentService.updateComment(eq(1L), eq(1L), any(CommentUpdatePutRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/posts/1/comments/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"수정된 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                    .andExpect(jsonPath("$.data.content").value("수정된 댓글"));
        }

        @Test
        @WithMockUser(username = "other@test.com")
        @DisplayName("작성자가 아닌 사용자가 수정 시 403")
        void updateComment_Forbidden() throws Exception {
            // given
            given(commentService.updateComment(eq(1L), eq(1L), any(CommentUpdatePutRequest.class), eq("other@test.com")))
                    .willThrow(new ForbiddenException("댓글 수정/삭제 권한이 없습니다."));

            // when & then
            mockMvc.perform(put("/api/v1/posts/1/comments/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"수정된 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("댓글 삭제 API")
    class DeleteComment {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("댓글 삭제 성공")
        void deleteComment_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/posts/1/comments/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
        }

        @Test
        @WithMockUser(username = "other@test.com")
        @DisplayName("작성자가 아닌 사용자가 삭제 시 403")
        void deleteComment_Forbidden() throws Exception {
            // given
            doThrow(new ForbiddenException("댓글 수정/삭제 권한이 없습니다."))
                    .when(commentService).deleteComment(eq(1L), eq(1L), eq("other@test.com"));

            // when & then
            mockMvc.perform(delete("/api/v1/posts/1/comments/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("답글 작성 API")
    class CreateReply {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("답글 작성 성공")
        void createReply_Success() throws Exception {
            // given
            ReplyPostResponse response = createReplyPostResponse(2L, "새 답글", 1L);
            given(commentService.createReply(eq(1L), eq(1L), any(ReplyCreatePostRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/posts/1/comments/1/replies")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"새 답글\"}"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("답글 작성 성공"))
                    .andExpect(jsonPath("$.data.replyId").value(2))
                    .andExpect(jsonPath("$.data.parentCommentId").value(1));
        }
    }

    @Nested
    @DisplayName("답글 수정 API")
    class UpdateReply {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("답글 수정 성공")
        void updateReply_Success() throws Exception {
            // given
            ReplyPutResponse response = createReplyPutResponse(2L, "수정된 답글", 1L);
            given(commentService.updateReply(eq(1L), eq(1L), eq(2L), any(ReplyUpdatePutRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/posts/1/comments/1/replies/2")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"수정된 답글\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("답글 수정 성공"))
                    .andExpect(jsonPath("$.data.content").value("수정된 답글"));
        }
    }

    @Nested
    @DisplayName("답글 삭제 API")
    class DeleteReply {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("답글 삭제 성공")
        void deleteReply_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/posts/1/comments/1/replies/2")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("답글 삭제 성공"));
        }
    }

    // === 헬퍼 메서드 ===

    private CommentWithRepliesGetResponse createCommentWithRepliesGetResponse(Long id, String content) {
        return CommentWithRepliesGetResponse.builder()
                .commentId(id)
                .content(content)
                .author(createAuthorResponse())
                .replies(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CommentPostResponse createCommentPostResponse(Long id, String content) {
        return CommentPostResponse.builder()
                .commentId(id)
                .content(content)
                .author(createAuthorResponse())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CommentPutResponse createCommentPutResponse(Long id, String content) {
        return CommentPutResponse.builder()
                .commentId(id)
                .content(content)
                .author(createAuthorResponse())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ReplyPostResponse createReplyPostResponse(Long id, String content, Long parentCommentId) {
        return ReplyPostResponse.builder()
                .replyId(id)
                .content(content)
                .author(createAuthorResponse())
                .parentCommentId(parentCommentId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ReplyPutResponse createReplyPutResponse(Long id, String content, Long parentCommentId) {
        return ReplyPutResponse.builder()
                .replyId(id)
                .content(content)
                .author(createAuthorResponse())
                .parentCommentId(parentCommentId)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AuthorResponse createAuthorResponse() {
        return AuthorResponse.builder()
                .userId(1L)
                .nickname("테스터")
                .build();
    }
}
