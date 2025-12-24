package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.TagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagMapRepository extends JpaRepository<TagMap, Long> {

    List<TagMap> findAllByPost(Post post);

    @Modifying
    @Query("DELETE FROM TagMap tm WHERE tm.post = :post")
    void deleteAllByPost(@Param("post") Post post);
}