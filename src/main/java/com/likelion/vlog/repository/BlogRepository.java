package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Blog;
import com.likelion.vlog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findByUser(User user);
}