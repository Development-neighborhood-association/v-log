package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Comment;
import com.likelion.vlog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostAndParentIsNull(Post post);

    int countByPost(Post post);

    // N+1 해결: 여러 Post의 댓글 수를 한번에 조회
    @Query("SELECT c.post.id, COUNT(c) FROM Comment c WHERE c.post IN :posts GROUP BY c.post.id")
    List<Object[]> countByPosts(@Param("posts") List<Post> posts);
}