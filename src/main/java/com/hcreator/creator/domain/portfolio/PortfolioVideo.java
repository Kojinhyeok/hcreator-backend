package com.hcreator.creator.domain.portfolio;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String category; // product-promo / ai-production / brand / viral / review

    @Column(length = 30)
    private String subLabel; // 예: "숏폼형", "가로형", "AI 드라마", "애니메이션", "표시광고법"

    @Column(length = 20)
    private String format; // vertical(숏폼형, 4열) / horizontal(가로형, 3열)

    private String title;
    private String thumbnailPath;
    private String linkUrl;
    private Integer sortOrder;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}