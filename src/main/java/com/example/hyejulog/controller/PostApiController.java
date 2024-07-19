package com.example.hyejulog.controller;

import com.example.hyejulog.domain.Post;
import com.example.hyejulog.jwt.util.JwtTokenizer;
import com.example.hyejulog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostService postService;
    private final JwtTokenizer jwtTokenizer;

    // 모든 페이징처리된 게시글
    @GetMapping()
    public Page<Post> getAllPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    // 게시글 등록 처리
    @PostMapping("/write")
    public ResponseEntity<Post> createPost(@RequestBody Post post, HttpServletRequest request) {
        // 게시글을 자신의 블로그에 올리기 위해 토큰에서 사용자 ID 추출
        String authorization = request.getHeader("Authorization");  // 헤더에서 JWT 토큰 추출
        Long userIdFromToken = jwtTokenizer.getUserIdFromToken(authorization);  // 토큰에서 사용자 ID 추출

        Post createdPost = postService.createPost(userIdFromToken, post);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }



}
