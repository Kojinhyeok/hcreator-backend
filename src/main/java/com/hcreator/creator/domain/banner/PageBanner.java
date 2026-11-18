package com.hcreator.creator.domain.banner;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 페이지별 배너/문구 설정. 값이 null이면 기본값(BannerDefaults)을 쓰고,
 * 빈 문자열("")이면 "비워 두기"(예: 영상 없음)를 뜻한다.
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_page_banner_key", columnNames = "pageKey"))
public class PageBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, nullable = false)
    private String pageKey;

    @Column(length = 200)
    private String label;

    @Column(length = 500)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String buttonText;

    @Column(length = 500)
    private String buttonLink;

    @Column(length = 100)
    private String button2Text;

    @Column(length = 500)
    private String button2Link;

    @Column(length = 1000)
    private String videoPath;

    @Column(length = 1000)
    private String imagePath;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}