package com.hcreator.creator.domain.gif;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GifTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String imagePath;

    private Integer sortOrder;

    @Column(columnDefinition = "LONGTEXT")
    private String detailContent;
}