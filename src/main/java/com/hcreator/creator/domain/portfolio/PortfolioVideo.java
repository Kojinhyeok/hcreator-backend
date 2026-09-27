package com.hcreator.creator.domain.portfolio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String groupType; // product-intro / review / shortform / ai-production

    private String title;
    private String thumbnailPath;
    private String linkUrl;
    private Integer sortOrder;
}