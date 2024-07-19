package com.example.hyejulog.repository;

import com.example.hyejulog.domain.Blog;
import com.example.hyejulog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByUser(User user);
}

