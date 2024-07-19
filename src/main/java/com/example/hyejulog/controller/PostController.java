package com.example.hyejulog.controller;

import com.example.hyejulog.domain.Post;
import com.example.hyejulog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    // 메인 페이지 (모든 게시글 조회)
    @GetMapping("/")
    public String getAllPosts(Pageable pageable, Model model) {
        Page<Post> posts = postService.getAllPosts(pageable);
        model.addAttribute("posts", posts);
        return "index";
    }

    // 게시글 상세 조회
    @GetMapping("/@{username}/{postId}")
    public String getPostById(@PathVariable String username, Long postId, Model model) {
        Post post = postService.getPostById(postId);
        model.addAttribute("post", post);
        return "blog/post-details";
    }

    // 게시글 등록
    @GetMapping("/write")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "blog/create-post";
    }

}
