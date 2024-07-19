package com.example.hyejulog.service;

import com.example.hyejulog.domain.Blog;
import com.example.hyejulog.domain.Post;
import com.example.hyejulog.repository.BlogRepository;
import com.example.hyejulog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<Post> getPostsByBlog(Blog blog, Pageable pageable) {
        return postRepository.findByBlog(blog, pageable);
    }


}
