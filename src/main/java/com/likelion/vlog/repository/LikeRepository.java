package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Like;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    int countByPost(Post post);

    boolean existsByUserAndPost(User user, Post post);

    Optional<Like> findByUserAndPost(User user, Post post);

    // N+1 해결: 여러 Post의 좋아요 수를 한번에 조회
    @Query("SELECT l.post.id, COUNT(l) FROM Like l WHERE l.post IN :posts GROUP BY l.post.id")
    List<Object[]> countByPosts(@Param("posts") List<Post> posts);
}