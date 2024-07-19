package com.example.hyejulog.service;

import com.example.hyejulog.domain.Blog;
import com.example.hyejulog.domain.Post;
import com.example.hyejulog.domain.User;
import com.example.hyejulog.exception.NotFoundException;
import com.example.hyejulog.repository.BlogRepository;
import com.example.hyejulog.repository.PostRepository;
import com.example.hyejulog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;

    // 모든 게시글 조회
    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
    }

    // 게시글 등록
    @Transactional
    public Post createPost(Long userId, Post post) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        Blog blog = blogRepository.findByUser(user).orElseThrow(() -> new NotFoundException("블로그를 찾을 수 없습니다."));
        post.setBlog(blog);
        return postRepository.save(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

}
