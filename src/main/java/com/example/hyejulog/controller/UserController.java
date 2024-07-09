package com.example.hyejulog.controller;

import com.example.hyejulog.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class UserController {

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }


    // 회원가입 폼
    @GetMapping("/userregform")
    public String userRegForm(Model model) {
        model.addAttribute("user", new User());
        return "users/userregform";
    }

    // 로그인 폼
    @GetMapping("/loginform")
    public String loginForm() {
        return "users/loginform";
    }
}
