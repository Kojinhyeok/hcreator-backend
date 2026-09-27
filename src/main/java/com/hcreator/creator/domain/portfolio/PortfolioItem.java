package com.hcreator.creator.domain.portfolio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String category; // ai-image / card-news / detail-page

    private String name;
    private String imagePath;
    private String linkUrl;
    private Integer sortOrder;
}