package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByBlogId(Long blogId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tagMapList tm JOIN tm.tag t WHERE t.title = :tagName")
    Page<Post> findAllByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tagMapList tm JOIN tm.tag t WHERE t.title = :tagName AND p.blog.id = :blogId")
    Page<Post> findAllByTagNameAndBlogId(@Param("tagName") String tagName, @Param("blogId") Long blogId, Pageable pageable);
}