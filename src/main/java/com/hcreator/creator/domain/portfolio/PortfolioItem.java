package com.hcreator.creator.domain.portfolio;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String category; // ai-image / card-news / detail-page

    @Column(length = 20)
    private String cardNewsLayout; // category=card-news 일 때만: basic / square

    private String name;
    private String imagePath;
    private String linkUrl;
    private Integer sortOrder;

    // category=detail-page 일 때만: 에디터로 이어붙인 상세 내용(HTML). 비어 있으면 imagePath 한 장으로 보여준다.
    @Column(columnDefinition = "LONGTEXT")
    private String detailContent;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}