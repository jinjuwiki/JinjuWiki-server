package com.jinju.jinjuwiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 애플리케이션 부트스트랩 및 스케줄링 활성화 클래스
@SpringBootApplication
@EnableScheduling
public class JinjuWikiApplication {

    // 애플리케이션 실행 메서드
    public static void main(String[] args) {
        SpringApplication.run(JinjuWikiApplication.class, args);
    }
}
