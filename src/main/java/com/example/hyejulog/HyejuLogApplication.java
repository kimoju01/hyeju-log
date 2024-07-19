package com.example.hyejulog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing  // 등록일, 수정일 자동 등록 기능 위해 JPA Auditing 활성화
@SpringBootApplication
public class HyejuLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(HyejuLogApplication.class, args);
    }

}
