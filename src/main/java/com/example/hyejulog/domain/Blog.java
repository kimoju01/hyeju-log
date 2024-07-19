package com.example.hyejulog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "blog")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)  // 등록일, 수정일 자동 등록
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String title;           // 블로그 제목

    @Column(columnDefinition = "longtext")    // 대용량 데이터
    private String introduction;    // 블로그 소개글

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // User 1 : 1 Blog. 연관 관계의 주인 = Blog
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Blog 1 : N Post. 블로그가 사라지면 게시글도 삭제된다.
    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

}
