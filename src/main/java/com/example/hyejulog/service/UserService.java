package com.example.hyejulog.service;

import com.example.hyejulog.domain.Blog;
import com.example.hyejulog.domain.Role;
import com.example.hyejulog.domain.User;
import com.example.hyejulog.repository.RoleRepository;
import com.example.hyejulog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 유저 회원가입
    @Transactional
    public User registerUser(User user) {
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("USER Role not found"));

        Blog blog = new Blog();
        blog.setTitle(user.getUsername());  // 최초 블로그 제목을 유저 아이디로 설정
        user.setBlog(blog);
        blog.setUser(user);

        user.setRoles(Collections.singleton(userRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));


        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }


}
