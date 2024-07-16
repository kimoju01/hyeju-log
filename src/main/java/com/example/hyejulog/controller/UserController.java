package com.example.hyejulog.controller;

import com.example.hyejulog.domain.User;
import com.example.hyejulog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "users/welcome";
    }

    // 회원가입 폼
    @GetMapping("/userregform")
    public String userRegForm(Model model) {
        model.addAttribute("user", new User());
        return "users/userregform";
    }

    // 회원가입 처리
    @PostMapping("/userreg")
    public String userReg(@Valid @ModelAttribute User user,
                          BindingResult bindingResult,
                          Model model) {

        if (bindingResult.hasErrors()) {
            return "users/userregform";
        }

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "duplicated", "이미 사용 중인 아이디입니다.");
            model.addAttribute("usernameError", "이미 사용 중인 아이디입니다.");
            return "users/error";
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "duplicated", "이미 사용 중인 이메일입니다.");
            model.addAttribute("emailError", "이미 사용 중인 이메일입니다.");
            return "users/error";
        }

        userService.registerUser(user);
        return "redirect:/welcome";
    }

    // 로그인 폼
    @GetMapping("/loginform")
    public String loginForm() {
        return "users/login";
    }


}
