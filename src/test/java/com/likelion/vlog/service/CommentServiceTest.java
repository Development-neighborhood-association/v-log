package com.likelion.vlog.service;

import com.likelion.vlog.dto.comments.*;
import com.likelion.vlog.entity.Blog;
import com.likelion.vlog.entity.Comment;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.CommentRepository;
import com.likelion.vlog.repository.PostRepository;
import com.likelion.vlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Blog blog;
    private Post post;
    private Comment comment;
    private Comment reply;

    @BeforeEach
    void setUp() throws Exception {
        // User 객체 생성 (리플렉션 사용)
        user = createUser(1L, "test@test.com", "테스터");

        blog = createBlog(1L, user);

        post = createPost(1L, "테스트 게시글", "테스트 내용", blog);

        comment = createComment(1L, user, post, null, "테스트 댓글");

        reply = createComment(2L, user, post, comment, "테스트 답글");
    }

    private User createUser(Long id, String email, String nickname) throws Exception {
        Constructor<User> constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        User user = constructor.newInstance();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "email", email);
        ReflectionTestUtils.setField(user, "nickname", nickname);
        return user;
    }

    private Blog createBlog(Long id, User user) throws Exception {
        Constructor<Blog> constructor = Blog.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Blog blog = constructor.newInstance();
        ReflectionTestUtils.setField(blog, "id", id);
        ReflectionTestUtils.setField(blog, "user", user);
        return blog;
    }

    private Post createPost(Long id, String title, String content, Blog blog) throws Exception {
        Constructor<Post> constructor = Post.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Post post = constructor.newInstance();
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "title", title);
        ReflectionTestUtils.setField(post, "content", content);
        ReflectionTestUtils.setField(post, "blog", blog);
        return post;
    }

    private Comment createComment(Long id, User user, Post post, Comment parent, String content) throws Exception {
        Constructor<Comment> constructor = Comment.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Comment comment = constructor.newInstance();
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "user", user);
        ReflectionTestUtils.setField(comment, "post", post);
        ReflectionTestUtils.setField(comment, "parent", parent);
        ReflectionTestUtils.setField(comment, "content", content);
        ReflectionTestUtils.setField(comment, "children", new ArrayList<Comment>());
        return comment;
    }

    @Nested
    @DisplayName("댓글 작성")
    class CreateComment {

        @Test
        @DisplayName("댓글 작성 성공")
        void createComment_Success() {
            // given
            CommentCreatePostRequest request = new CommentCreatePostRequest();
            ReflectionTestUtils.setField(request, "content", "새 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            // when
            CommentPostResponse response = commentService.createComment(1L, request, "test@test.com");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCommentId()).isEqualTo(1L);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 작성 시 실패")
        void createComment_PostNotFound() {
            // given
            CommentCreatePostRequest request = new CommentCreatePostRequest();
            ReflectionTestUtils.setField(request, "content", "새 댓글");

            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(999L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("댓글 수정 성공")
        void updateComment_Success() {
            // given
            CommentUpdatePutRequest request = new CommentUpdatePutRequest();
            ReflectionTestUtils.setField(request, "content", "수정된 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when
            CommentPutResponse response = commentService.updateComment(1L, 1L, request, "test@test.com");

            // then
            assertThat(response).isNotNull();
            assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 수정 시 실패")
        void updateComment_Forbidden() {
            // given
            CommentUpdatePutRequest request = new CommentUpdatePutRequest();
            ReflectionTestUtils.setField(request, "content", "수정된 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(1L, 1L, request, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("댓글 삭제 성공")
        void deleteComment_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when
            commentService.deleteComment(1L, 1L, "test@test.com");

            // then
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 삭제 시 실패")
        void deleteComment_Forbidden() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(1L, 1L, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("답글 작성")
    class CreateReply {

        @Test
        @DisplayName("답글 작성 성공")
        void createReply_Success() {
            // given
            ReplyCreatePostRequest request = new ReplyCreatePostRequest();
            ReflectionTestUtils.setField(request, "content", "새 답글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(commentRepository.save(any(Comment.class))).willReturn(reply);

            // when
            ReplyPostResponse response = commentService.createReply(1L, 1L, request, "test@test.com");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReplyId()).isEqualTo(2L);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("답글에 답글 작성 시 실패")
        void createReply_ToReply_Fail() {
            // given
            ReplyCreatePostRequest request = new ReplyCreatePostRequest();
            ReflectionTestUtils.setField(request, "content", "새 답글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(2L)).willReturn(Optional.of(reply));

            // when & then
            assertThatThrownBy(() -> commentService.createReply(1L, 2L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("답글 수정")
    class UpdateReply {

        @Test
        @DisplayName("답글 수정 성공")
        void updateReply_Success() {
            // given
            ReplyUpdatePutRequest request = new ReplyUpdatePutRequest();
            ReflectionTestUtils.setField(request, "content", "수정된 답글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
            given(commentRepository.findById(2L)).willReturn(Optional.of(reply));

            // when
            ReplyPutResponse response = commentService.updateReply(1L, 1L, 2L, request, "test@test.com");

            // then
            assertThat(response).isNotNull();
            assertThat(reply.getContent()).isEqualTo("수정된 답글");
        }
    }

    @Nested
    @DisplayName("답글 삭제")
    class DeleteReply {

        @Test
        @DisplayName("답글 삭제 성공")
        void deleteReply_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
            given(commentRepository.findById(2L)).willReturn(Optional.of(reply));

            // when
            commentService.deleteReply(1L, 1L, 2L, "test@test.com");

            // then
            verify(commentRepository).delete(reply);
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("댓글 목록 조회 성공")
        void getComments_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findAllByPostWithChildren(post)).willReturn(List.of(comment));

            // when
            List<CommentWithRepliesGetResponse> responses = commentService.getComments(1L);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCommentId()).isEqualTo(1L);
        }
    }
}
