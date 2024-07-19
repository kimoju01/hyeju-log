package com.example.hyejulog.repository;

import com.example.hyejulog.domain.Blog;
import com.example.hyejulog.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBlog(Blog blog, Pageable pageable);
}
