package com.hcreator.creator.domain.section;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ServiceDetailSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String pageType; // "product" / "detail-page" / "blog" / "video"

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imagePath;

    private Integer sortOrder;
}