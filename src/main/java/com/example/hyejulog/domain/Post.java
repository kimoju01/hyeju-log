package com.example.hyejulog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)  // 등록일, 수정일 자동 등록
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;       // 글 제목

    @Column(columnDefinition = "longtext", nullable = false)
    private String content;     // 글 내용

    @Column(nullable = false)
    private boolean isTemp = false;     // 임시글 유무

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Post N : 1 Blog. 연관 관계의 주인 = Post가 Many니까 Post가 관계의 주인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    // Post N : 1 Category. 연관 관계의 주인 = Post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 게시글이 삭제되면 게시글 이미지도 삭제된다.
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images;

}
